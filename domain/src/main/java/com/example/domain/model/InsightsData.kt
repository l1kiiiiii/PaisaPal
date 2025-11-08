package com.example.domain.model

data class InsightsData(
    val totalSpent: Double,
    val totalIncome: Double,
    val categoryBreakdown: List<CategorySpending>,
    val monthlyTrend: List<MonthlySpending>,
    val topMerchants: List<MerchantSpending>
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class MonthlySpending(
    val month: String,
    val amount: Double,
    val date: Long
)

data class MerchantSpending(
    val merchantName: String,
    val amount: Double,
    val transactionCount: Int
)
