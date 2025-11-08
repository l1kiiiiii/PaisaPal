package com.example.domain.usecase

import com.example.domain.model.*
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.*

class GetBudgetSummaryUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend fun execute(): BudgetSummary {
        val budgets = budgetRepository.getAllActiveBudgets().first()
        val transactions = transactionRepository.getAllTransactions().first()

        // Calculate spent amounts per category
        val categorySpending = calculateCategorySpending(transactions)

        // Update budgets with current spending
        val updatedBudgets = budgets.map { budget ->
            val spent = categorySpending[budget.category] ?: 0.0
            budget.copy(spentAmount = spent)
        }

        val totalBudget = budgets.sumOf { it.limitAmount }
        val totalSpent = categorySpending.values.sum()
        val overBudgetCount = updatedBudgets.count { it.isOverBudget }
        val nearLimitCount = updatedBudgets.count { it.isNearLimit && !it.isOverBudget }

        return BudgetSummary(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            categoryBudgets = updatedBudgets,
            overBudgetCount = overBudgetCount,
            nearLimitCount = nearLimitCount
        )
    }

    private fun calculateCategorySpending(transactions: List<Transaction>): Map<String, Double> {
        val now = System.currentTimeMillis()
        val monthStart = getMonthStart(now)

        return transactions
            .filter { it.type == TransactionType.DEBIT }
            .filter { it.timestamp >= monthStart }
            .filter { it.category != null }
            .groupBy { it.category!! }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    }

    private fun getMonthStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
