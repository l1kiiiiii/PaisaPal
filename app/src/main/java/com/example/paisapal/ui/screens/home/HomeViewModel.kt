package com.example.paisapal.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @RequiresApi(Build.VERSION_CODES.O)
    val homeFeed: StateFlow<List<HomeFeedItem>> = transactions
        .map { txns -> buildSmartFeed(txns) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun buildSmartFeed(transactions: List<Transaction>): List<HomeFeedItem> {
        val feed = mutableListOf<HomeFeedItem>()

        // Calculate current month stats
        val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val monthStart = now.withDayOfMonth(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val thisMonthTransactions = transactions.filter { it.timestamp >= monthStart }
        val totalSpent = thisMonthTransactions
            .filter { it.type == com.example.domain.model.TransactionType.DEBIT }
            .sumOf { it.amount }

        val needsReview = transactions.count { it.category == null }

        // Add Overview Card
        feed.add(
            HomeFeedItem.OverviewCard(
                totalSpent = totalSpent,
                needsReview = needsReview,
                budgetStatus = "Food",
                budgetProgress = 0.8f
            )
        )

        // Add Budget Alert if exceeded
        val foodBudgetLimit = 15000.0
        val foodSpent = thisMonthTransactions
            .filter { it.category == "Food & Dining" }
            .sumOf { it.amount }

        if (foodSpent > foodBudgetLimit) {
            feed.add(
                HomeFeedItem.BudgetAlert(
                    category = "Food & Dining",
                    spent = foodSpent,
                    limit = foodBudgetLimit,
                    isExceeded = true
                )
            )
        }

        // Add Review Prompt for first uncategorized transaction
        val uncategorized = transactions.firstOrNull { it.category == null }
        uncategorized?.let {
            feed.add(
                HomeFeedItem.ReviewPrompt(
                    transactionId = it.id,
                    amount = it.amount,
                    timestamp = it.timestamp,
                    suggestedCategory = guessCategoryByTime(it.timestamp)
                )
            )
        }

        // Add recent transactions
        transactions.take(10).forEach { txn ->
            feed.add(HomeFeedItem.TransactionItem(txn))
        }

        return feed
    }

    private fun guessCategoryByTime(timestamp: Long): String {
        val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
            ).hour
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        return when (hour) {
            in 6..10 -> "Food & Dining"  // Morning = breakfast
            in 12..14 -> "Food & Dining" // Lunch
            in 18..22 -> "Food & Dining" // Dinner
            in 22..24, in 0..2 -> "Entertainment" // Late night
            else -> "Shopping"
        }
    }

}
