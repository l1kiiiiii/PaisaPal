// app/src/main/java/com/example/paisapal/ui/screens/detail/TransactionDetailViewModel.kt
package com.example.paisapal.ui.screens.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import com.example.domain.repository.UserCorrectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userCorrectionRepository: UserCorrectionRepository
) : ViewModel() {

    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?> = _transaction.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _categoryUpdated = MutableStateFlow(false)
    val categoryUpdated: StateFlow<Boolean> = _categoryUpdated.asStateFlow()

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getTransactionByIdFlow(transactionId)
                    .catch { e ->
                        Log.e(TAG, "Error loading transaction", e)
                        _error.value = "Failed to load transaction"
                        _isLoading.value = false
                    }
                    .collect { transaction ->
                        _transaction.value = transaction
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error loading transaction", e)
                _error.value = "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(newCategory: String) {
        viewModelScope.launch {
            try {
                val currentTransaction = _transaction.value
                if (currentTransaction == null) {
                    _error.value = "No transaction loaded"
                    return@launch
                }

                // Update transaction in database
                val updatedTransaction = currentTransaction.copy(
                    category = newCategory,
                    needsReview = false
                )

                repository.update(updatedTransaction)

                // Learn from user correction for future transactions
                val merchantName = currentTransaction.merchantDisplayName
                    ?: currentTransaction.merchantRaw
                    ?: currentTransaction.upiVpa?.substringBefore("@")

                if (merchantName != null) {
                    userCorrectionRepository.saveCorrection(merchantName, newCategory)
                    Log.d(TAG, "Saved correction: $merchantName -> $newCategory")
                }

                _categoryUpdated.value = true
                Log.d(TAG, "Category updated to: $newCategory")

            } catch (e: Exception) {
                Log.e(TAG, "Error updating category", e)
                _error.value = "Failed to update category"
            }
        }
    }

    // Legacy method for backward compatibility (if needed)
    @Deprecated("Use updateCategory(newCategory) instead", ReplaceWith("updateCategory(newCategory)"))
    fun updateCategory(transactionId: String, newCategory: String) {
        if (_transaction.value?.id == transactionId) {
            updateCategory(newCategory)
        } else {
            viewModelScope.launch {
                try {
                    repository.updateTransactionCategory(transactionId, newCategory)
                    Log.d(TAG, "Category updated to: $newCategory")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating category", e)
                    _error.value = "Failed to update category"
                }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.delete(transaction)
                Log.d(TAG, "Transaction deleted: ${transaction.id}")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting transaction", e)
                _error.value = "Failed to delete transaction"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetCategoryUpdated() {
        _categoryUpdated.value = false
    }

    companion object {
        private const val TAG = "TransactionDetailVM"
    }
}
