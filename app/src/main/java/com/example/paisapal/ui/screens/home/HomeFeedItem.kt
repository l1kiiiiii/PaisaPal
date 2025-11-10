package com.example.paisapal.ui.screens.home

import com.example.domain.model.Transaction

sealed class HomeFeedItem {
    data class OverviewCard(
        val totalSpent: Double,
        val needsReview: Int,
        val budgetStatus: String,
        val budgetProgress: Float
    ) : HomeFeedItem()

    data class TransactionItem(
        val transaction: Transaction
    ) : HomeFeedItem()

    data class BudgetAlert(
        val category: String,
        val spent: Double,
        val limit: Double,
        val isExceeded: Boolean
    ) : HomeFeedItem()

    data class ReviewPrompt(
        val transactionId: String,
        val amount: Double,
        val timestamp: Long,
        val suggestedCategory: String?
    ) : HomeFeedItem()
}
