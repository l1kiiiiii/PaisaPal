package com.example.paisapal.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.data.NotificationCache
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.BudgetSummary
import com.example.domain.usecase.GetBudgetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase,
    private val notificationCache: NotificationCache
) : ViewModel() {

    private val GLOBAL_CATEGORY_KEY = "GLOBAL_SALARY"

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _smartFeedItems = MutableStateFlow<List<SmartFeedItem>>(emptyList())
    val smartFeedItems: StateFlow<List<SmartFeedItem>> = _smartFeedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Combine Transactions, Budget Summaries, and Global Salary
                combine(
                    repository.getAllTransactions(),
                    getBudgetSummaryUseCase(),
                    flow { emit(budgetRepository.getBudgetByCategory(GLOBAL_CATEGORY_KEY)) }
                ) { transactions, budgetSummaries, globalSalaryBudget ->
                    Triple(transactions, budgetSummaries, globalSalaryBudget)
                }
                    .catch { e ->
                        if (e is CancellationException) throw e
                        Log.e(TAG, "Error loading data", e)
                        _error.value = "Failed to load data"
                        _isLoading.value = false
                    }
                    .collect { (transactions, budgetSummaries, globalSalaryBudget) ->
                        _transactions.value = transactions

                        // Pass global salary to the builder
                        val salaryAmount = globalSalaryBudget?.limitAmount ?: 0.0
                        _smartFeedItems.value = buildSmartFeed(transactions, budgetSummaries, salaryAmount)

                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Fatal error in loadData", e)
                _error.value = "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    fun addManualTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.insert(transaction)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding transaction", e)
                _error.value = "Failed to add transaction"
            }
        }
    }

    private fun buildSmartFeed(
        transactions: List<Transaction>,
        budgetSummaries: List<BudgetSummary>,
        salaryAmount: Double
    ): List<SmartFeedItem> {
        val feedItems = mutableListOf<SmartFeedItem>()

        // Sum of all negative transactions (Debits)
        val totalSpentValue = transactions
            .filter { it.type == TransactionType.DEBIT }
            .sumOf { it.amount }

        // 1. Overview Card Logic
        if (salaryAmount > 0) {
            // SCENARIO A: Salary is set
            val progress = (totalSpentValue / salaryAmount).toFloat()
            val remaining = salaryAmount - totalSpentValue

            val statusText = if (remaining < 0) {
                "Overspending by ₹${abs(remaining.toInt())}"
            } else {
                "₹${remaining.toInt()} safe to spend"
            }

            feedItems.add(
                SmartFeedItem.OverviewCard(
                    totalSpent = totalSpentValue,
                    budgetStatus = statusText,
                    budgetProgress = progress
                )
            )
        } else {
            // SCENARIO B: No Salary set (Fallback to Category Health)
            val criticalBudget = budgetSummaries
                .filter { it.category != GLOBAL_CATEGORY_KEY }
                .filter { it.progress >= 0.8f }
                .maxByOrNull { it.progress }

            if (criticalBudget != null) {
                feedItems.add(
                    SmartFeedItem.OverviewCard(
                        totalSpent = totalSpentValue,
                        budgetStatus = "${criticalBudget.category} is critical",
                        budgetProgress = criticalBudget.progress
                    )
                )
            } else {
                feedItems.add(
                    SmartFeedItem.OverviewCard(
                        totalSpent = totalSpentValue,
                        budgetStatus = "All categories on track",
                        budgetProgress = 0f
                    )
                )
            }
        } //  ( Logic below runs for EVERYONE now)

        // 2. Needs Review Section
        val needsReviewTransactions = transactions.filter { it.needsReview }
        if (needsReviewTransactions.isNotEmpty()) {
            feedItems.add(SmartFeedItem.NeedsReviewBanner(count = needsReviewTransactions.size))
        }

        //  3. Budget Alerts (Exclude Global Salary from alerts list)
        val overBudgetCategories = budgetSummaries
            .filter { it.category != GLOBAL_CATEGORY_KEY }
            .filter { it.isOverBudget }

        if (overBudgetCategories.isNotEmpty()) {
            val first = overBudgetCategories.first()
            feedItems.add(
                SmartFeedItem.BudgetAlert(
                    category = first.category,
                    overage = first.spentAmount - first.budgetAmount
                )
            )
        }

        //  4. Recent Transactions
        val recentTransactions = transactions
            .sortedByDescending { it.timestamp }
            .take(5)

        if (recentTransactions.isNotEmpty()) {
            feedItems.add(SmartFeedItem.TransactionSection(recentTransactions))
        }

        return feedItems
    }

    fun refreshData() {
        loadData()
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}

// Smart Feed Item Types
sealed class SmartFeedItem {
    data class OverviewCard(
        val totalSpent: Double,
        val budgetStatus: String,
        val budgetProgress: Float
    ) : SmartFeedItem()

    data class NeedsReviewBanner(val count: Int) : SmartFeedItem()
    data class TransactionSection(val transactions: List<Transaction>) : SmartFeedItem()
    data class BudgetAlert(
        val category: String,
        val overage: Double
    ) : SmartFeedItem()
}