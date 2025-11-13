package com.example.paisapal.ui.screens.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//  ReviewItem data class
data class ReviewItem(
    val transaction: Transaction,
    val suggestedCategory: String,
    val confidence: Float = 0.8f
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uncategorizedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val uncategorizedTransactions: StateFlow<List<Transaction>> = _uncategorizedTransactions.asStateFlow()

    //  reviewItems with suggestions
    val reviewItems: StateFlow<List<ReviewItem>> = _uncategorizedTransactions.map { transactions ->
        transactions.map { transaction ->
            ReviewItem(
                transaction = transaction,
                suggestedCategory = suggestCategory(transaction)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    //  Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadUncategorizedTransactions()
    }

    private fun loadUncategorizedTransactions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.getUncategorizedTransactions()
                    .catch { e ->
                        Log.e(TAG, "Error loading uncategorized transactions", e)
                        _error.value = "Failed to load transactions"
                        _isLoading.value = false
                    }
                    .collect { transactions ->
                        _uncategorizedTransactions.value = transactions
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error", e)
                _error.value = "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    //  confirmCategory method
    fun confirmCategory(transactionId: String, category: String, merchantKeyword: String?) {
        viewModelScope.launch {
            try {
                repository.updateTransactionCategory(transactionId, category)
                _snackbarMessage.value = "Categorized as '$category'"
                Log.d(TAG, "Transaction categorized: $category")

                // TODO: Store merchant keyword for future learning
                merchantKeyword?.let {
                    // Save to learning database
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error categorizing transaction", e)
                _error.value = "Failed to categorize transaction"
            }
        }
    }

    fun categorizeTransaction(transactionId: String, category: String) {
        confirmCategory(transactionId, category, null)
    }

    //  Category suggestion logic
    private fun suggestCategory(transaction: Transaction): String {
        // Simple keyword-based suggestion
        val merchantName = transaction.merchantDisplayName?.lowercase() ?: ""
        val smsBody = transaction.smsBody.lowercase()

        return when {
            // Food & Dining
            merchantName.contains("zomato") || merchantName.contains("swiggy") ||
                    merchantName.contains("restaurant") || smsBody.contains("food") -> "Food & Dining"

            // Shopping
            merchantName.contains("amazon") || merchantName.contains("flipkart") ||
                    merchantName.contains("myntra") || smsBody.contains("shopping") -> "Shopping"

            // Transportation
            merchantName.contains("uber") || merchantName.contains("ola") ||
                    merchantName.contains("rapido") || smsBody.contains("ride") -> "Transportation"

            // Entertainment
            merchantName.contains("netflix") || merchantName.contains("hotstar") ||
                    merchantName.contains("spotify") || merchantName.contains("movie") -> "Entertainment"

            // Bills & Utilities
            smsBody.contains("electricity") || smsBody.contains("water") ||
                    smsBody.contains("bill") || smsBody.contains("recharge") -> "Bills & Utilities"

            // Healthcare
            merchantName.contains("pharma") || merchantName.contains("hospital") ||
                    smsBody.contains("medicine") || smsBody.contains("doctor") -> "Healthcare"

            // Education
            merchantName.contains("course") || merchantName.contains("udemy") ||
                    smsBody.contains("tuition") || smsBody.contains("education") -> "Education"

            // Default
            else -> "Others"
        }
    }

    fun clearError() {
        _error.value = null
    }

    //  clearSnackbar method
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        private const val TAG = "ReviewViewModel"
    }
}
