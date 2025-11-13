package com.example.paisapal.ui.screens.budget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Budget
import com.example.domain.model.BudgetPeriod
import com.example.domain.repository.BudgetRepository
import com.example.domain.usecase.BudgetSummary
import com.example.domain.usecase.GetBudgetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                getBudgetSummaryUseCase()
                    .catch { e ->
                        Log.e(TAG, "Error loading budgets", e)
                        _error.value = "Failed to load budgets: ${e.message}"
                        _isLoading.value = false
                    }
                    .collect { summaries ->
                        _budgetSummaries.value = summaries
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error loading budgets", e)
                _error.value = "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    //  Use insertBudget instead of insert
    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.insertBudget(budget)
                Log.d(TAG, "Budget added: ${budget.category}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding budget", e)
                _error.value = "Failed to add budget"
            }
        }
    }

    fun createBudget(
        category: String,
        amount: Double,
        alertThreshold: Float = 0.8f
    ) {
        viewModelScope.launch {
            try {
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
                Log.d(TAG, "Budget created: $category")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating budget", e)
                _error.value = "Failed to create budget"
            }
        }
    }

    //  Use updateBudget instead of update
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.updateBudget(budget)
                Log.d(TAG, "Budget updated: ${budget.category}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating budget", e)
                _error.value = "Failed to update budget"
            }
        }
    }

    //  Use deleteBudget instead of delete
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budget)
                Log.d(TAG, "Budget deleted: ${budget.category}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting budget", e)
                _error.value = "Failed to delete budget"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        private const val TAG = "BudgetViewModel"
    }
}
