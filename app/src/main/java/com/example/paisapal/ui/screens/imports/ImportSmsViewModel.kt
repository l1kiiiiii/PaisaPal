package com.example.paisapal.ui.screens.imports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SmsContentProvider
import com.example.domain.engine.CategorizationEngine
import com.example.domain.engine.TransactionParser
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportSmsViewModel @Inject constructor(
    private val smsProvider: SmsContentProvider,
    private val parser: TransactionParser,
    private val categorizationEngine: CategorizationEngine,
    private val repository: TransactionRepository
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun importAllSms() {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                // Get existing transactions to check for duplicates
                val existingTransactions = repository.getAllTransactions().first()
                val existingSmsIds = existingTransactions.map {
                    "${it.sender}_${it.timestamp}_${it.amount}"
                }.toSet()

                // Read all SMS
                val allSms = smsProvider.readAllSms()
                Log.d(TAG, "Found ${allSms.size} SMS messages")

                var successCount = 0
                var failureCount = 0
                var duplicateCount = 0

                // Parse each SMS
                allSms.forEach { sms ->
                    // Create unique ID for duplicate detection
                    val smsId = "${sms.address}_${sms.date}_${extractAmount(sms.body)}"

                    // Skip if already imported
                    if (existingSmsIds.contains(smsId)) {
                        duplicateCount++
                        return@forEach
                    }

                    val transaction = parser.parse(sms.body, sms.address, sms.date)

                    if (transaction != null) {
                        // ✅ AUTO-CATEGORIZE BEFORE SAVING
                        val category = categorizationEngine.categorize(transaction)
                        val categorizedTransaction = transaction.copy(
                            category = category,
                            needsReview = category == null
                        )

                        repository.insert(categorizedTransaction)
                        successCount++

                        Log.d(TAG, "Imported: ${transaction.amount} - Category: ${category ?: "Uncategorized"}")
                    } else {
                        failureCount++
                    }
                }

                _importState.value = ImportState.Success(
                    imported = successCount,
                    failed = failureCount,
                    duplicates = duplicateCount,
                    total = allSms.size
                )

                Log.d(TAG, "Import complete: $successCount imported, $failureCount failed, $duplicateCount duplicates skipped")
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun importBankSmsOnly() {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                // Get existing transactions to check for duplicates
                val existingTransactions = repository.getAllTransactions().first()
                val existingSmsIds = existingTransactions.map {
                    "${it.sender}_${it.timestamp}_${it.amount}"
                }.toSet()

                // Read bank SMS from last 30 days
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                val allSms = smsProvider.readSmsSince(thirtyDaysAgo)

                Log.d(TAG, "Found ${allSms.size} SMS from last 30 days")

                var successCount = 0
                var failureCount = 0
                var duplicateCount = 0

                allSms.forEach { sms ->
                    // Check if it's from a bank
                    if (isBankSms(sms.address)) {
                        // Create unique ID for duplicate detection
                        val smsId = "${sms.address}_${sms.date}_${extractAmount(sms.body)}"

                        // Skip if already imported
                        if (existingSmsIds.contains(smsId)) {
                            duplicateCount++
                            return@forEach
                        }

                        val transaction = parser.parse(sms.body, sms.address, sms.date)

                        if (transaction != null) {
                            // ✅ AUTO-CATEGORIZE BEFORE SAVING
                            val category = categorizationEngine.categorize(transaction)
                            val categorizedTransaction = transaction.copy(
                                category = category,
                                needsReview = category == null
                            )

                            repository.insert(categorizedTransaction)
                            successCount++

                            Log.d(TAG, "Imported: ${transaction.merchantDisplayName ?: "Unknown"} - Rs.${transaction.amount} - Category: ${category ?: "Uncategorized"}")
                        } else {
                            failureCount++
                        }
                    }
                }

                _importState.value = ImportState.Success(
                    imported = successCount,
                    failed = failureCount,
                    duplicates = duplicateCount,
                    total = allSms.size
                )

                Log.d(TAG, "Bank SMS import complete: $successCount imported, $failureCount failed, $duplicateCount duplicates skipped")
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun isBankSms(sender: String): Boolean {
        val bankKeywords = listOf(
            "bank", "hdfc", "icici", "sbi", "axis", "kotak", "indus",
            "paytm", "gpay", "phonepe", "amazon", "googlepay", "bhim"
        )
        return bankKeywords.any { sender.contains(it, ignoreCase = true) }
    }

    // Extract amount from SMS for duplicate detection
    private fun extractAmount(smsBody: String): String {
        val amountPattern = """(?:Rs\.?|INR)\s*[:=]?\s*([\d,]+\.?\d*)""".toRegex(RegexOption.IGNORE_CASE)
        val match = amountPattern.find(smsBody)
        return match?.groupValues?.get(1)?.replace(",", "") ?: "0"
    }

    companion object {
        private const val TAG = "ImportSmsViewModel"
    }
}

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Success(
        val imported: Int,
        val failed: Int,
        val duplicates: Int,
        val total: Int
    ) : ImportState()
    data class Error(val message: String) : ImportState()
}
