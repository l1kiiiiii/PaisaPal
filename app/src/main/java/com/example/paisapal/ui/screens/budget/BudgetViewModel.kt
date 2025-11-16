package com.example.paisapal.ui.screens.budget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.data.MerchantRegistry
import com.example.domain.model.Budget
import com.example.domain.model.BudgetPeriod
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.TransactionRepository
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
    private val transactionRepository: TransactionRepository,
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase
) : ViewModel() {

    private val _budgetSummaries = MutableStateFlow<List<BudgetSummary>>(emptyList())
    val budgetSummaries: StateFlow<List<BudgetSummary>> = _budgetSummaries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Available categories from existing transactions
    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    init {
        loadBudgets()
        loadAvailableCategories()
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

    private fun loadAvailableCategories() {
        viewModelScope.launch {
            try {
                // Get all unique categories from transactions
                transactionRepository.getAllTransactions()
                    .map { transactions ->
                        transactions
                            .mapNotNull { it.category }
                            .distinct()
                            .sorted()
                    }
                    .catch { e ->
                        Log.e(TAG, "Error loading categories", e)
                        // Fallback to predefined categories from MerchantRegistry
                        emit(getPredefinedCategories())
                    }
                    .collect { categories ->
                        // Combine transaction categories with predefined ones
                        val allCategories = (categories + getPredefinedCategories())
                            .distinct()
                            .sorted()
                        _availableCategories.value = allCategories
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories", e)
                _availableCategories.value = getPredefinedCategories()
            }
        }
    }

    private fun getPredefinedCategories(): List<String> {
        return listOf(
            "Food & Dining",
            "Shopping",
            "Groceries",
            "Transportation",
            "Entertainment",
            "Utilities",
            "Health & Fitness",
            "Education",
            "Travel",
            "Bills & Recharges",
            "Investment",
            "Transfer",
            "Others"
        )
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

    fun deleteBudgetByCategory(category: String) {
        viewModelScope.launch {
            try {
                val budget = budgetRepository.getBudgetByCategory(category)
                budget?.let {
                    budgetRepository.deleteBudget(it)
                    Log.d(TAG, "Budget deleted: $category")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting budget", e)
                _error.value = "Failed to delete budget"
            }
        }
    }

    fun showEditDialog(category: String) {
        // Implement edit functionality if needed
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        private const val TAG = "BudgetViewModel"
    }
}
