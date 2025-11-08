package com.example.domain.usecase

import com.example.domain.model.Budget
import com.example.domain.model.BudgetSummary
import com.example.domain.usecase.GetBudgetSummaryUseCase

class CheckBudgetAlertsUseCase(
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase
) {

    suspend fun execute(): List<BudgetAlert> {
        val summary = getBudgetSummaryUseCase.execute()
        val alerts = mutableListOf<BudgetAlert>()

        summary.categoryBudgets.forEach { budget ->
            when {
                budget.isOverBudget -> {
                    alerts.add(
                        BudgetAlert(
                            category = budget.category,
                            type = AlertType.OVER_BUDGET,
                            message = "You've exceeded your ${budget.category} budget by ₹${String.format("%.0f", budget.spentAmount - budget.limitAmount)}"
                        )
                    )
                }
                budget.isNearLimit -> {
                    alerts.add(
                        BudgetAlert(
                            category = budget.category,
                            type = AlertType.NEAR_LIMIT,
                            message = "${budget.category}: ${String.format("%.0f", budget.usagePercentage)}% of budget used"
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
