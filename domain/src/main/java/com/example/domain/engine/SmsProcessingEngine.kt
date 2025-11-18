
package com.example.domain.engine

import com.example.domain.model.SmsMessage
import com.example.domain.model.Transaction
import javax.inject.Inject
import java.util.UUID

class SmsProcessingEngine @Inject constructor(
    private val senderAuthentication: SenderAuthentication,
    private val transactionParser: TransactionParser,
    private val categorizationEngine: CategorizationEngine,
    private val contextEngine: ContextEngine
) {
    suspend fun processSms(smsMessage: SmsMessage): Transaction? {
        if (!senderAuthentication.isAuthentic(smsMessage.address)) {
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

        // NEW: Use confidence-based categorization
        val categorizationResult = categorizationEngine.categorizeWithConfidence(transaction)

        transaction = transaction.copy(
            category = categorizationResult.category,
            needsReview = categorizationResult.confidence < 0.70f, // Flag for review if low confidence
            contextConfidence = categorizationResult.confidence
        )

        val enriched = contextEngine.enrichWithContext(transaction)
        return enriched.transaction
    }

}
