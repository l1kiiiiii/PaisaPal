package com.example.paisapal.ui.screens.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.MerchantMapping
import com.example.domain.model.Transaction
import com.example.domain.repository.MerchantMappingRepository
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ReviewItem(
    val transaction: Transaction,
    val suggestedCategory: String,
    val confidence: Float = 0.8f
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val merchantMappingRepository: MerchantMappingRepository
) : ViewModel() {

    private val _uncategorizedTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    //  Load learned mappings
    private val learnedMappings: StateFlow<List<MerchantMapping>> =
        merchantMappingRepository.getAllMappings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val reviewItems: StateFlow<List<ReviewItem>> = combine(
        _uncategorizedTransactions,
        learnedMappings
    ) { transactions, mappings ->
        transactions.map { transaction ->
            ReviewItem(
                transaction = transaction,
                suggestedCategory = suggestCategoryWithLearning(transaction, mappings)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadUncategorizedTransactions()
    }

    private fun loadUncategorizedTransactions() {
        viewModelScope.launch {
            try {
                repository.getUncategorizedTransactions()
                    .catch { e ->
                        Log.e(TAG, "Error loading transactions", e)
                    }
                    .collect { transactions ->
                        _uncategorizedTransactions.value = transactions
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error", e)
            }
        }
    }

    //  Save learning when user confirms
    fun confirmCategory(transactionId: String, category: String, merchantKeyword: String?) {
        viewModelScope.launch {
            try {
                // Update transaction
                repository.updateTransactionCategory(transactionId, category)

                //  Save learning
                merchantKeyword?.let { keyword ->
                    val existingMapping = merchantMappingRepository.getMappingByKeyword(keyword)

                    if (existingMapping != null) {
                        // Increment usage count
                        merchantMappingRepository.incrementUsageCount(keyword)
                    } else {
                        // Create new mapping
                        val newMapping = MerchantMapping(
                            id = UUID.randomUUID().toString(),
                            merchantKeyword = keyword.uppercase(),
                            category = category,
                            userConfirmed = true,
                            usageCount = 1,
                            lastUsed = System.currentTimeMillis(),
                            createdAt = System.currentTimeMillis()
                        )
                        merchantMappingRepository.insertMapping(newMapping)
                    }
                }

                _snackbarMessage.value = "Categorized as '$category' ✓"
                Log.d(TAG, "Transaction categorized and learned: $category")

            } catch (e: Exception) {
                Log.e(TAG, "Error categorizing transaction", e)
                _snackbarMessage.value = "Failed to categorize"
            }
        }
    }

    //  IMPROVED: Use learned mappings first
    private fun suggestCategoryWithLearning(
        transaction: Transaction,
        learnedMappings: List<MerchantMapping>
    ): String {
        val merchantName = transaction.merchantDisplayName?.uppercase() ?: ""
        val merchantRaw = transaction.merchantRaw?.uppercase() ?: ""

        // 1. Check learned mappings first (highest priority)
        learnedMappings.forEach { mapping ->
            if (merchantName.contains(mapping.merchantKeyword) ||
                merchantRaw.contains(mapping.merchantKeyword)) {
                return mapping.category
            }
        }

        // 2. Fall back to keyword-based suggestions
        return suggestCategoryByKeywords(transaction)
    }

    private fun suggestCategoryByKeywords(transaction: Transaction): String {
        val merchantName = transaction.merchantDisplayName?.lowercase() ?: ""
        val smsBody = transaction.smsBody.lowercase()

        return when {
            merchantName.contains("zomato") || merchantName.contains("swiggy") ||
                    merchantName.contains("restaurant") || smsBody.contains("food") -> "Food & Dining"

            merchantName.contains("amazon") || merchantName.contains("flipkart") ||
                    merchantName.contains("myntra") || smsBody.contains("shopping") -> "Shopping"

            merchantName.contains("uber") || merchantName.contains("ola") ||
                    merchantName.contains("rapido") || smsBody.contains("ride") -> "Transportation"

            merchantName.contains("netflix") || merchantName.contains("hotstar") ||
                    merchantName.contains("spotify") || merchantName.contains("movie") -> "Entertainment"

            smsBody.contains("electricity") || smsBody.contains("water") ||
                    smsBody.contains("bill") || smsBody.contains("recharge") -> "Bills & Utilities"

            merchantName.contains("pharma") || merchantName.contains("hospital") ||
                    smsBody.contains("medicine") || smsBody.contains("doctor") -> "Healthcare"

            merchantName.contains("course") || merchantName.contains("udemy") ||
                    smsBody.contains("tuition") || smsBody.contains("education") -> "Education"

            else -> "Others"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        private const val TAG = "ReviewViewModel"
    }
}
