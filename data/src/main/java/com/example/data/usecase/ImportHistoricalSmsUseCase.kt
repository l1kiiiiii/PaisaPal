package com.example.data.usecase

import com.example.domain.engine.TransactionParser
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository

class ImportHistoricalSmsUseCase(
    private val repository: TransactionRepository,
    private val parser: TransactionParser
) {

    suspend fun execute(smsList: List<SmsData>): ImportResult {
        val transactions = mutableListOf<Transaction>()
        var successCount = 0
        var failureCount = 0

        smsList.forEach { sms ->
            val transaction = parser.parse(sms.body, sms.sender, sms.timestamp)

            if (transaction != null) {
                transactions.add(transaction)
                successCount++
            } else {
                failureCount++
            }
        }

        // Insert all at once
        if (transactions.isNotEmpty()) {
            repository.insertMultiple(transactions)
        }

        return ImportResult(
            successCount = successCount,
            failureCount = failureCount,
            totalProcessed = smsList.size
        )
    }
}

data class ImportResult(
    val successCount: Int,
    val failureCount: Int,
    val totalProcessed: Int
)

data class SmsData(
    val sender: String,
    val body: String,
    val timestamp: Long
)
