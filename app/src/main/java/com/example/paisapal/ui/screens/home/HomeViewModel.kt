package com.example.paisapal.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.GetBudgetSummaryUseCase
import com.example.domain.usecase.BudgetSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _smartFeedItems = MutableStateFlow<List<SmartFeedItem>>(emptyList())
    val smartFeedItems: StateFlow<List<SmartFeedItem>> = _smartFeedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Start as true
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Combine both flows and update UI when either changes
                combine(
                    repository.getAllTransactions(),
                    getBudgetSummaryUseCase()
                ) { transactions, budgetSummaries ->
                    _transactions.value = transactions
                    buildSmartFeed(transactions, budgetSummaries)
                }.collect { smartFeed ->
                    _smartFeedItems.value = smartFeed
                    _isLoading.value = false // Set to false after first emission
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading data", e)
                _isLoading.value = false
            }
        }
    }

    fun addManualTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                // Data will auto-refresh via Flow
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error adding transaction", e)
            }
        }
    }

    private fun buildSmartFeed(
        transactions: List<Transaction>,
        budgetSummaries: List<BudgetSummary>
    ): List<SmartFeedItem> {
        val feedItems = mutableListOf<SmartFeedItem>()

        // 1. Overview Card with real budget data
        val criticalBudget = budgetSummaries
            .filter { it.progress >= 0.8f }
            .maxByOrNull { it.progress }

        if (criticalBudget != null) {
            feedItems.add(
                SmartFeedItem.OverviewCard(
                    totalSpent = transactions
                        .filter { it.type == com.example.domain.model.TransactionType.DEBIT }
                        .sumOf { it.amount },
                    budgetStatus = criticalBudget.category,
                    budgetProgress = criticalBudget.progress
                )
            )
        } else {
            feedItems.add(
                SmartFeedItem.OverviewCard(
                    totalSpent = transactions
                        .filter { it.type == com.example.domain.model.TransactionType.DEBIT }
                        .sumOf { it.amount },
                    budgetStatus = "All categories on track",
                    budgetProgress = 0f
                )
            )
        }

        // 2. Needs Review Section
        val needsReviewTransactions = transactions.filter { it.needsReview }
        if (needsReviewTransactions.isNotEmpty()) {
            feedItems.add(
                SmartFeedItem.NeedsReviewBanner(count = needsReviewTransactions.size)
            )
        }

        // 3. Recent Transactions (last 7 days)
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val recentTransactions = transactions
            .filter { it.timestamp >= sevenDaysAgo }
            .sortedByDescending { it.timestamp }
            .take(10)

        if (recentTransactions.isNotEmpty()) {
            feedItems.add(SmartFeedItem.TransactionSection(recentTransactions))
        }

        // 4. Budget Alerts (over budget categories)
        val overBudgetCategories = budgetSummaries.filter { it.isOverBudget }
        if (overBudgetCategories.isNotEmpty()) {
            feedItems.add(
                SmartFeedItem.BudgetAlert(
                    category = overBudgetCategories.first().category,
                    overage = overBudgetCategories.first().spentAmount - overBudgetCategories.first().budgetAmount
                )
            )
        }

        return feedItems
    }

    fun refreshData() {
        loadData()
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
