package com.example.domain.usecase


import com.example.domain.repository.SmsRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImportHistoricalSmsUseCase @Inject constructor(
    private val smsRepository: SmsRepository,
    private val transactionRepository: TransactionRepository
) {
    sealed class ImportProgress {
        data class Scanning(
            val scanned: Int,
            val total: Int,
            val found: Int,
            val categorized: Int
        ) : ImportProgress()

        data class Complete(
            val imported: Int,
            val duplicates: Int,
            val failed: Int,
            val total: Int
        ) : ImportProgress()

        data class Error(val message: String) : ImportProgress()
    }

    operator fun invoke(onlyBankSms: Boolean = true): Flow<ImportProgress> = flow {
        try {
            // Get all SMS messages
            val messages = smsRepository.getAllSmsMessages(onlyBankSms)
            val totalMessages = messages.size

            var scannedCount = 0
            var foundCount = 0
            var categorizedCount = 0
            var importedCount = 0
            var duplicatesCount = 0
            var failedCount = 0

            messages.forEach { smsMessage ->
                scannedCount++

                // Emit progress every 10 messages
                if (scannedCount % 10 == 0 || scannedCount == totalMessages) {
                    emit(
                        ImportProgress.Scanning(
                            scanned = scannedCount,
                            total = totalMessages,
                            found = foundCount,
                            categorized = categorizedCount
                        )
                    )
                }

                // Try to parse the SMS into a transaction
                val transaction = smsRepository.parseTransaction(smsMessage)

                if (transaction != null) {
                    foundCount++

                    // Check if transaction already exists
                    val exists = transactionRepository.transactionExists(transaction.id)

                    if (!exists) {
                        // Save the transaction
                        transactionRepository.insertTransaction(transaction)
                        importedCount++

                        if (transaction.category != null) {
                            categorizedCount++
                        }
                    } else {
                        duplicatesCount++
                    }
                } else {
                    failedCount++
                }
            }

            // Emit final completion
            emit(
                ImportProgress.Complete(
                    imported = importedCount,
                    duplicates = duplicatesCount,
                    failed = failedCount,
                    total = totalMessages
                )
            )
        } catch (e: Exception) {
            emit(ImportProgress.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
