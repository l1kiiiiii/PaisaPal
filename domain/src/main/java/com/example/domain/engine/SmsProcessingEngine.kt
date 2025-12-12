package com.example.domain.engine

import com.example.domain.model.SmsMessage
import com.example.domain.model.Transaction
import com.example.domain.repository.UserAccount
import com.example.domain.repository.UserAccountsRepository
import javax.inject.Inject
import java.util.UUID

class SmsProcessingEngine @Inject constructor(
    private val senderAuthentication: SenderAuthentication,
    private val transactionParser: TransactionParser,
    private val categorizationEngine: CategorizationEngine,
    private val contextEngine: ContextEngine,
    private val userAccountsRepository: UserAccountsRepository
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

        //   Find which account matches and keep it
        val matchedAccount =
            findMatchingAccount(smsMessage.body) ?: return null  // No user account found in message

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
            needsReview = false,

            accountLast4Digits = matchedAccount.last4Digits,
            accountName = matchedAccount.accountName
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

    //  Returns the matched account instead of just boolean
    private suspend fun findMatchingAccount(messageBody: String): UserAccount? {
        val userAccounts = userAccountsRepository.getAllAccounts()

        // If no accounts configured, return null (require accounts now)
        if (userAccounts.isEmpty()) {
            // For backward compatibility, you could return a default account:
            // return UserAccount("0000", "Default Account")
            return null
        }

        // Extract all 4-digit numbers from message
        val numbersInMessage = Regex("\\d{4}").findAll(messageBody)
            .map { it.value }
            .toSet()

        // Return the FIRST matching account
        return userAccounts.firstOrNull { account ->
            numbersInMessage.contains(account.last4Digits)
        }
    }
}
