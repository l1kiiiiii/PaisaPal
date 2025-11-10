package com.example.paisapal.ui.screens.review


import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewItem(
    val transaction: Transaction,
    val suggestedCategory: String
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: TransactionRepository
    // TODO: Inject MerchantMappingRepository when available
) : ViewModel() {

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val reviewItems: StateFlow<List<ReviewItem>> = repository.getAllTransactions()
        .map { transactions ->
            transactions
                .filter { it.category == null }
                .map { transaction ->
                    ReviewItem(
                        transaction = transaction,
                        suggestedCategory = suggestCategory(transaction)
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun confirmCategory(transactionId: String, category: String, merchantKeyword: String?) {
        viewModelScope.launch {
            // Update transaction category
            repository.updateTransactionCategory(transactionId, category)

            // Save merchant mapping rule if keyword exists
            merchantKeyword?.let {
                // TODO: repository.saveMerchantMapping(it, category)
            }

            _snackbarMessage.value = "✅ Got it! I'll remember that for next time."
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun suggestCategory(transaction: Transaction): String {
        // Time-based suggestion
        val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(transaction.timestamp),
                java.time.ZoneId.systemDefault()
            ).hour
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        return when {
            hour in 6..10 -> "Food & Dining"
            hour in 12..14 -> "Food & Dining"
            hour in 18..22 -> "Food & Dining"
            hour in 22..24 || hour in 0..2 -> "Entertainment"
            transaction.amount > 5000 -> "Shopping"
            else -> "Others"
        }
    }
}
