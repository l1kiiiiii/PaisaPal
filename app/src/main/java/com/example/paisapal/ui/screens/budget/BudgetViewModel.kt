package com.example.paisapal.ui.screens.budget


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Budget
import com.example.domain.model.BudgetPeriod
import com.example.domain.model.BudgetSummary
import com.example.domain.repository.BudgetRepository
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

    private val _budgetSummary = MutableStateFlow<BudgetSummary?>(null)
    val budgetSummary: StateFlow<BudgetSummary?> = _budgetSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val summary = getBudgetSummaryUseCase.execute()
                _budgetSummary.value = summary
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                id = UUID.randomUUID().toString(),
                category = category,
                limitAmount = amount,
                spentAmount = 0.0,
                period = BudgetPeriod.MONTHLY
            )
            budgetRepository.insertBudget(budget)
            loadBudgets()
        }
    }
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
            loadBudgets() // Refresh
        }
    }
}
