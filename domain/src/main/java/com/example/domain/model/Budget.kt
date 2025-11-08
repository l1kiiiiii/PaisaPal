package com.example.domain.model

data class Budget(
    val id: String,
    val category: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val period: BudgetPeriod,
    val alertThreshold: Int = 80, // Alert at 80% usage
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double
        get() = limitAmount - spentAmount

    val usagePercentage: Float
        get() = if (limitAmount > 0) ((spentAmount / limitAmount) * 100).toFloat() else 0f

    val isOverBudget: Boolean
        get() = spentAmount > limitAmount

    val isNearLimit: Boolean
        get() = usagePercentage >= alertThreshold
}

enum class BudgetPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

data class BudgetSummary(
    val totalBudget: Double,
    val totalSpent: Double,
    val categoryBudgets: List<Budget>,
    val overBudgetCount: Int,
    val nearLimitCount: Int
) {
    val remainingBudget: Double
        get() = totalBudget - totalSpent

    val usagePercentage: Float
        get() = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toFloat() else 0f
}
