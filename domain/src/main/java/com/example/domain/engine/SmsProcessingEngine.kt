// domain/src/main/java/com/example/domain/engine/SmsProcessingEngine.kt
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
        // Step 1: Verify sender
        if (!senderAuthentication.isAuthentic(smsMessage.sender)) {
            return null
        }

        // Step 2: Parse transaction
        // IMPORTANT: Match the EXACT parameter names from YOUR TransactionParser.parse() method
        val parsedTransaction = transactionParser.parse(
            smsMessage.body,     // First parameter: body
            smsMessage.sender,   // Second parameter: sender
            smsMessage.timestamp // Third parameter: timestamp
        ) ?: return null

        // Step 3: Convert ParsedTransaction to Transaction
        var transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = parsedTransaction.amount,
            type = parsedTransaction.type,
            merchantRaw = parsedTransaction.merchantRaw,
            merchantDisplayName = null,
            category = null,
            timestamp = parsedTransaction.timestamp,
            smsBody = smsMessage.body,
            sender = smsMessage.sender,
            referenceNumber = parsedTransaction.referenceNumber,
            upiVpa = parsedTransaction.upiVpa,
            needsReview = false
        )

        // Step 4: Auto-categorize
        val category = categorizationEngine.categorize(transaction)
        transaction = transaction.copy(
            category = category,
            needsReview = category == null
        )

        // Step 5: Enrich with context
        val enriched = contextEngine.enrichWithContext(transaction)

        return enriched.transaction
    }
}
