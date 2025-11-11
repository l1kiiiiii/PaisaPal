package com.example.domain.usecase

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckBudgetAlertsUseCase @Inject constructor(
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase
) {

    suspend fun execute(): List<BudgetAlert> {
        // Get the first emission from the Flow
        val budgetSummaries = getBudgetSummaryUseCase().first()
        val alerts = mutableListOf<BudgetAlert>()

        budgetSummaries.forEach { budget ->
            when {
                budget.isOverBudget -> {
                    alerts.add(
                        BudgetAlert(
                            category = budget.category,
                            type = AlertType.OVER_BUDGET,
                            message = "You've exceeded your ${budget.category} budget by ₹${String.format("%.0f", budget.spentAmount - budget.budgetAmount)}"
                        )
                    )
                }
                budget.progress >= 0.8f && !budget.isOverBudget -> {
                    alerts.add(
                        BudgetAlert(
                            category = budget.category,
                            type = AlertType.NEAR_LIMIT,
                            message = "${budget.category}: ${String.format("%.0f", budget.progress * 100)}% of budget used"
                        )
                    )
                }
            }
        }

        return alerts
    }
}

data class BudgetAlert(
    val category: String,
    val type: AlertType,
    val message: String
)

enum class AlertType {
    NEAR_LIMIT,
    OVER_BUDGET
}
