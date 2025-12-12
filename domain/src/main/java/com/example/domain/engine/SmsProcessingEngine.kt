package com.example.domain.engine

import com.example.domain.model.SmsMessage
import com.example.domain.model.Transaction
import com.example.domain.repository.UserAccountsRepository  // ✅ ADD THIS
import javax.inject.Inject
import java.util.UUID

class SmsProcessingEngine @Inject constructor(
    private val senderAuthentication: SenderAuthentication,
    private val transactionParser: TransactionParser,
    private val categorizationEngine: CategorizationEngine,
    private val contextEngine: ContextEngine,
    private val userAccountsRepository: UserAccountsRepository  // ✅ ADD THIS
) {
    suspend fun processSms(smsMessage: SmsMessage): Transaction? {
        // Step 1: Check if sender is authentic
        if (!senderAuthentication.isAuthentic(smsMessage.address)) {
            return null
        }

        // Step 2: Check if sender is spam
        if (senderAuthentication.isSpam(smsMessage.address)) {
            return null
        }

        //  Step 3: Verify message contains user's account number
        if (!containsUserAccount(smsMessage.body)) {
            return null
        }

        val parsedTransaction = transactionParser.parse(
            smsMessage.body,
            smsMessage.address,
            smsMessage.timestamp
        ) ?: return null

        var transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = parsedTransaction.amount,
            type = parsedTransaction.type,
            merchantRaw = parsedTransaction.merchantRaw,
            merchantDisplayName = null,
            category = null,
            timestamp = parsedTransaction.timestamp,
            smsBody = smsMessage.body,
            sender = smsMessage.address,
            referenceNumber = parsedTransaction.referenceNumber,
            upiVpa = parsedTransaction.upiVpa,
            needsReview = false
        )

        // Use confidence-based categorization
        val categorizationResult = categorizationEngine.categorizeWithConfidence(transaction)

        transaction = transaction.copy(
            category = categorizationResult.category,
            needsReview = categorizationResult.confidence < 0.70f,
            contextConfidence = categorizationResult.confidence
        )

        val enriched = contextEngine.enrichWithContext(transaction)
        return enriched.transaction
    }


    private suspend fun containsUserAccount(messageBody: String): Boolean {
        val userAccounts = userAccountsRepository.getAllAccounts()

        // If no accounts configured, allow all (backward compatibility)
        if (userAccounts.isEmpty()) {
            return true
        }

        // Extract all 4-digit numbers from message
        val numbersInMessage = Regex("\\d{4}").findAll(messageBody)
            .map { it.value }
            .toSet()

        // Check if ANY user account matches
        return userAccounts.any { account ->
            numbersInMessage.contains(account.last4Digits)
        }
    }
}
