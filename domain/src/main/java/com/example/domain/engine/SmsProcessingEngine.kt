package com.example.domain.engine

import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import java.util.UUID

class SmsProcessingEngine(
    private val transactionParser: TransactionParser,
    private val categorizationEngine: CategorizationEngine,
    private val contextEngine: ContextEngine,
    private val transactionRepository: TransactionRepository
) {

    suspend fun processIncomingSms(
        sender: String,
        body: String,
        timestamp: Long
    ): ProcessingResult {
        // Stage 1 & 2: Parse the SMS
        val parsedData = transactionParser.parse(body, sender, timestamp)
            ?: return ProcessingResult.Ignored("Not a transaction SMS")

        // Create initial transaction object
        var transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = parsedData.amount,
            type = parsedData.type,
            timestamp = parsedData.timestamp,
            merchantRaw = parsedData.merchantRaw,
            merchantDisplayName = parsedData.merchantRaw,
            upiVpa = parsedData.upiVpa,
            referenceNumber = parsedData.referenceNumber,
            sender = sender,
            smsBody = body,
            category = null,
            needsReview = true
        )

        // Stage 3: Try direct categorization
        // Pass the transaction object to match existing signature
        val category = categorizationEngine.categorize(transaction)

        if (category != null) {
            transaction = transaction.copy(
                category = category,
                merchantDisplayName = parsedData.merchantRaw ?: "Unknown",
                needsReview = false
            )
            transactionRepository.insertTransaction(transaction)
            return ProcessingResult.Success(transaction, "Direct Match")
        }

        // Stage 4: Try context enrichment (notification + location)
        val contextMatch = contextEngine.enrichWithContext(transaction)
        if (contextMatch != null) {
            transaction = transaction.copy(
                category = contextMatch.category,
                merchantDisplayName = contextMatch.merchantName,
                needsReview = false
            )
            transactionRepository.insertTransaction(transaction)
            return ProcessingResult.Success(transaction, "Context Match")
        }

        // Stage 5: Needs review
        transactionRepository.insertTransaction(transaction)
        return ProcessingResult.NeedsReview(transaction)
    }

    sealed class ProcessingResult {
        data class Success(val transaction: Transaction, val stage: String) : ProcessingResult()
        data class NeedsReview(val transaction: Transaction) : ProcessingResult()
        data class Ignored(val reason: String) : ProcessingResult()
    }
}
