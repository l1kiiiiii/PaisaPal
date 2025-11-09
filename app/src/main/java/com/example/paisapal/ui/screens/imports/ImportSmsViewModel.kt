package com.example.paisapal.ui.screens.imports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SmsContentProvider
import com.example.domain.engine.CategorizationEngine
import com.example.domain.engine.TransactionParser
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.ImportHistoricalSmsUseCase
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
    private val categorizationEngine: CategorizationEngine,
    private val repository: TransactionRepository,
    private val importHistoricalSmsUseCase:
    ImportHistoricalSmsUseCase
) : ViewModel() {

    sealed class ImportState {
        object Idle : ImportState()
        object PermissionExplanation : ImportState()

        data class Loading(
            val messagesScanned: Int = 0,
            val totalMessages: Int = 0,
            val transactionsFound: Int = 0,
            val categorized: Int = 0
        ) : ImportState()

        data class Success(
            val imported: Int,
            val duplicates: Int,
            val failed: Int,
            val total: Int
        ) : ImportState()

        data class Error(val message: String) : ImportState()
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.PermissionExplanation)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun acknowledgePermissionExplanation() {
        _importState.value = ImportState.Idle
    }

    fun importBankSmsOnly() {
        viewModelScope.launch {
            _importState.value = ImportState.Loading()
            try {
                importHistoricalSmsUseCase(onlyBankSms = true).collect { progress ->
                    when (progress) {
                        is com.example.domain.usecase.ImportHistoricalSmsUseCase.ImportProgress.Scanning -> {
                            _importState.value = ImportState.Loading(
                                messagesScanned = progress.scanned,
                                totalMessages = progress.total,
                                transactionsFound = progress.found,
                                categorized = progress.categorized
                            )
                        }
                        is com.example.domain.usecase.ImportHistoricalSmsUseCase.ImportProgress.Complete -> {
                            _importState.value = ImportState.Success(
                                imported = progress.imported,
                                duplicates = progress.duplicates,
                                failed = progress.failed,
                                total = progress.total
                            )
                        }
                        is com.example.domain.usecase.ImportHistoricalSmsUseCase.ImportProgress.Error -> {
                            _importState.value = ImportState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Import failed")
            }
        }
    }

    fun importAllSms() {
        viewModelScope.launch {
            _importState.value = ImportState.Loading()
            try {
                importHistoricalSmsUseCase(onlyBankSms = false).collect { progress ->
                    when (progress) {
                        is com.example.domain.usecase.ImportHistoricalSmsUseCase.ImportProgress.Scanning -> {
                            _importState.value = ImportState.Loading(
                                messagesScanned = progress.scanned,
                                totalMessages = progress.total,
                                transactionsFound = progress.found,
                                categorized = progress.categorized
                            )
                        }
                        is com.example.domain.usecase.ImportHistoricalSmsUseCase.ImportProgress.Complete -> {
                            _importState.value = ImportState.Success(
                                imported = progress.imported,
                                duplicates = progress.duplicates,
                                failed = progress.failed,
                                total = progress.total
                            )
                        }
                        is com.example.domain.usecase.ImportHistoricalSmsUseCase.ImportProgress.Error -> {
                            _importState.value = ImportState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Import failed")
            }
        }
    }
}
