package com.example.paisapal.ui.screens.imports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SmsContentProvider
import com.example.domain.engine.TransactionParser
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportSmsViewModel @Inject constructor(
    private val smsProvider: SmsContentProvider,
    private val parser: TransactionParser,
    private val repository: TransactionRepository
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun importAllSms() {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                // Read all SMS
                val allSms = smsProvider.readAllSms()
                Log.d(TAG, "Found ${allSms.size} SMS messages")

                var successCount = 0
                var failureCount = 0

                // Parse each SMS
                allSms.forEach { sms ->
                    val transaction = parser.parse(sms.body, sms.sender, sms.timestamp)

                    if (transaction != null) {
                        repository.insert(transaction)
                        successCount++
                    } else {
                        failureCount++
                    }
                }

                _importState.value = ImportState.Success(
                    imported = successCount,
                    failed = failureCount,
                    total = allSms.size
                )

                Log.d(TAG, "Import complete: $successCount imported, $failureCount failed")
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
                // Read only bank SMS (from last 30 days)
                val bankSms = smsProvider.readSmsFromLastDays(30)
                Log.d(TAG, "Found ${bankSms.size} bank SMS from last 30 days")

                var successCount = 0
                var failureCount = 0

                bankSms.forEach { sms ->
                    // Check if it's from a bank
                    if (isBankSms(sms.sender)) {
                        val transaction = parser.parse(sms.body, sms.sender, sms.timestamp)

                        if (transaction != null) {
                            repository.insert(transaction)
                            successCount++
                        } else {
                            failureCount++
                        }
                    }
                }

                _importState.value = ImportState.Success(
                    imported = successCount,
                    failed = failureCount,
                    total = bankSms.size
                )

                Log.d(TAG, "Bank SMS import complete: $successCount imported, $failureCount failed")
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun isBankSms(sender: String): Boolean {
        val bankKeywords = listOf("bank", "hdfc", "icici", "sbi", "axis", "paytm", "gpay", "phonepe")
        return bankKeywords.any { sender.contains(it, ignoreCase = true) }
    }

    companion object {
        private const val TAG = "ImportSmsViewModel"
    }
}

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Success(val imported: Int, val failed: Int, val total: Int) : ImportState()
    data class Error(val message: String) : ImportState()
}
