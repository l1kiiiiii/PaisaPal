package com.example.domain.usecase

import com.example.domain.model.Budget
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class BudgetSummary(
    val category: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progress: Float, // 0.0 to 1.0
    val isOverBudget: Boolean
)

class GetBudgetSummaryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {

    operator fun invoke(): Flow<List<BudgetSummary>> {
        return combine(
            budgetRepository.getAllActiveBudgets(), // Changed from getAllBudgets()
            transactionRepository.getAllTransactions()
        ) { budgets, transactions ->

            val currentMonth = getCurrentMonthRange()

            budgets.map { budget ->
                // Calculate spending for this budget category in current month
                val spent = transactions
                    .filter { transaction ->
                        transaction.category == budget.category &&
                                transaction.timestamp >= currentMonth.start &&
                                transaction.timestamp <= currentMonth.end
                    }
                    .sumOf { it.amount }

                val remaining = budget.limitAmount - spent // Changed from budget.amount
                val progress = if (budget.limitAmount > 0) {
                    (spent / budget.limitAmount).coerceIn(0.0, 1.0).toFloat()
                } else 0f

                BudgetSummary(
                    category = budget.category,
                    budgetAmount = budget.limitAmount, // Changed from budget.amount
                    spentAmount = spent,
                    remainingAmount = remaining,
                    progress = progress,
                    isOverBudget = spent > budget.limitAmount // Changed from budget.amount
                )
            }
        }
    }

    private fun getCurrentMonthRange(): MonthRange {
        val calendar = java.util.Calendar.getInstance()

        // Start of month
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val start = calendar.timeInMillis

        // End of month
        calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        val end = calendar.timeInMillis

        return MonthRange(start, end)
    }

    private data class MonthRange(val start: Long, val end: Long)
}
