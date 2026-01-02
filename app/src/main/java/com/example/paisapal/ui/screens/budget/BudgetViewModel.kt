package com.example.paisapal.ui.screens.budget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Budget
import com.example.domain.model.BudgetPeriod
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.BudgetSummary
import com.example.domain.usecase.GetBudgetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase
) : ViewModel() {

    private val GLOBAL_CATEGORY_KEY = "GLOBAL_SALARY"

    // 1. EXPENSE CATEGORIES
    private val _budgetSummaries = MutableStateFlow<List<BudgetSummary>>(emptyList())
    val budgetSummaries: StateFlow<List<BudgetSummary>> = _budgetSummaries.asStateFlow()


    // 2. GLOBAL SALARY (Income)
    private val _globalBudget = MutableStateFlow(0.0)
    val globalBudget: StateFlow<Double> = _globalBudget.asStateFlow()

    // 3. STATS
    private val _totalAllocated = MutableStateFlow(0.0)
    val totalAllocated: StateFlow<Double> = _totalAllocated.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Available categories from existing transactions
    private val _availableCategories = MutableStateFlow<List<String>>(getPredefinedCategories())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private var loadJob: Job? = null

    init {
        refreshData()
    }
    fun refreshData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadBudgets()
            loadAvailableCategories()
        }
    }
    private suspend fun loadBudgets() {
        try {
            _isLoading.value = true
            _error.value = null

            getBudgetSummaryUseCase()
                .catch { e ->
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Error loading budgets", e)
                    _error.value = "Failed to load budgets: ${e.message}"
                    _isLoading.value = false
                }
                .collect { summaries ->
                    // 1. Extract Global Salary
                    val globalSummary = summaries.find { it.category == GLOBAL_CATEGORY_KEY }
                    _globalBudget.value = globalSummary?.budgetAmount ?: 0.0

                    // 2. Filter Expenses (Remove Salary from the list)
                    val expenses = summaries.filter { it.category != GLOBAL_CATEGORY_KEY }
                    _budgetSummaries.value = expenses

                    // 3. Calculate Total Allocated
                    _totalAllocated.value = expenses.sumOf { it.budgetAmount }

                    _isLoading.value = false
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Fatal error loading budgets", e)
            _error.value = "An unexpected error occurred"
            _isLoading.value = false
        }
    }
    //  Logic to Set/Update Salary
    fun setGlobalBudget(amount: Double) {
        viewModelScope.launch {
            try {
                // Check if salary entry already exists to update it
                val existingSalary = budgetRepository.getBudgetByCategory(GLOBAL_CATEGORY_KEY)

                val budget = if (existingSalary != null) {
                    existingSalary.copy(limitAmount = amount)
                } else {
                    Budget(
                        id = UUID.randomUUID().toString(),
                        category = GLOBAL_CATEGORY_KEY,
                        limitAmount = amount,
                        spentAmount = 0.0,
                        period = BudgetPeriod.MONTHLY,
                        alertThreshold = 1.0f, // No alert for income
                        isActive = true,
                        createdAt = System.currentTimeMillis()
                    )
                }

                if (existingSalary != null) {
                    budgetRepository.updateBudget(budget)
                } else {
                    budgetRepository.insertBudget(budget)
                }

                // Refresh to update UI
                refreshData()

            } catch (e: Exception) {
                Log.e(TAG, "Error setting global budget", e)
                _error.value = "Failed to save salary"
            }
        }
    }

    // ✅ Updated: Refresh after create
    fun createBudget(category: String, amount: Double, alertThreshold: Float = 0.8f) {
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

                refreshData() // Refresh so it appears immediately

            } catch (e: Exception) {
                Log.e(TAG, "Error creating budget", e)
                _error.value = "Failed to create budget"
            }
        }
    }

    private suspend fun loadAvailableCategories() {
        try {
            transactionRepository.getAllTransactions()
                .map { transactions ->
                    transactions
                        .mapNotNull { it.category }
                        .distinct()
                        .sorted()
                }
                .catch { emit(getPredefinedCategories())}
                .collect { categories ->
                    val allCategories = (categories + getPredefinedCategories())
                        .distinct()
                        .sorted()
                    _availableCategories.value = allCategories
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error loading categories", e)
            _availableCategories.value = getPredefinedCategories()
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
