package com.example.paisapal.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Budget
import com.example.domain.model.BudgetPeriod
import com.example.domain.repository.BudgetRepository
import com.example.domain.usecase.BudgetSummary
import com.example.domain.usecase.GetBudgetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase
) : ViewModel() {

    private val _budgetSummaries = MutableStateFlow<List<BudgetSummary>>(emptyList())
    val budgetSummaries: StateFlow<List<BudgetSummary>> = _budgetSummaries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // GetBudgetSummaryUseCase returns a Flow, so we collect it
                getBudgetSummaryUseCase().collect { summaries ->
                    _budgetSummaries.value = summaries
                }
            } catch (e: Exception) {
                // Handle error
                _budgetSummaries.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBudget(
        category: String,
        amount: Double,
        alertThreshold: Float = 0.8f // Default to 80% threshold
    ) {
        viewModelScope.launch {
            val budget = Budget(
                id = UUID.randomUUID().toString(),
                category = category,
                limitAmount = amount,
                spentAmount = 0.0,
                period = BudgetPeriod.MONTHLY,
                alertThreshold = alertThreshold,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            budgetRepository.insertBudget(budget)
            loadBudgets()
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
            loadBudgets()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
            loadBudgets()
        }
    }

    fun toggleBudgetActive(budget: Budget) {
        viewModelScope.launch {
            val updated = budget.copy(isActive = !budget.isActive)
            budgetRepository.updateBudget(updated)
            loadBudgets()
        }
    }
}
