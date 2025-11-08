package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.entity.BudgetEntity
import com.example.domain.model.Budget
import com.example.domain.model.BudgetPeriod
import com.example.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAllActiveBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllActiveBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBudgetByCategory(category: String): Budget? {
        return budgetDao.getBudgetByCategory(category)?.toDomain()
    }

    override suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    private fun BudgetEntity.toDomain(): Budget {
        return Budget(
            id = id,
            category = category,
            limitAmount = limitAmount,
            spentAmount = 0.0, // Will be calculated from transactions
            period = BudgetPeriod.valueOf(period),
            alertThreshold = alertThreshold,
            isActive = isActive,
            createdAt = createdAt
        )
    }

    private fun Budget.toEntity(): BudgetEntity {
        return BudgetEntity(
            id = id,
            category = category,
            limitAmount = limitAmount,
            period = period.name,
            alertThreshold = alertThreshold,
            isActive = isActive,
            createdAt = createdAt
        )
    }
}
