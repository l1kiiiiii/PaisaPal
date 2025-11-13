package com.example.paisapal.ui.screens.insights

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class TimeFrame {
    WEEKLY, MONTHLY, YEARLY
}

data class CategorySpending(
    val category: String,
    val amount: Double
)

data class DailySpending(
    val date: String, // Changed from LocalDate to String for compatibility
    val amount: Double
)

data class InsightsState(
    val timeFrame: TimeFrame = TimeFrame.MONTHLY,
    val categorySpending: List<CategorySpending> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val transactionCount: Int = 0
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _timeFrame = MutableStateFlow(TimeFrame.MONTHLY)

    //  ADD ERROR HANDLING
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .catch { e ->
            Log.e(TAG, "Error loading transactions", e)
            _error.value = "Failed to load insights data"
            emit(emptyList())
        }
        .onStart { _isLoading.value = true }
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val insightsState: StateFlow<InsightsState> = combine(
        transactions,
        _timeFrame
    ) { txns, timeFrame ->
        try {
            val filtered = filterByTimeFrame(txns, timeFrame)
            InsightsState(
                timeFrame = timeFrame,
                categorySpending = calculateCategorySpending(filtered),
                dailySpending = calculateDailySpending(filtered, timeFrame),
                totalSpent = filtered.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
                totalIncome = filtered.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
                transactionCount = filtered.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating insights", e)
            _error.value = "Failed to calculate insights"
            InsightsState() // Return empty state on error
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsState()
    )

    fun setTimeFrame(timeFrame: TimeFrame) {
        _timeFrame.value = timeFrame
    }

    private fun filterByTimeFrame(transactions: List<Transaction>, timeFrame: TimeFrame): List<Transaction> {
        val now = System.currentTimeMillis()
        val cutoff = when (timeFrame) {
            TimeFrame.WEEKLY -> now - (7 * 24 * 60 * 60 * 1000L)
            TimeFrame.MONTHLY -> now - (30 * 24 * 60 * 60 * 1000L)
            TimeFrame.YEARLY -> now - (365 * 24 * 60 * 60 * 1000L)
        }
        return transactions.filter { it.timestamp >= cutoff }
    }

    private fun calculateCategorySpending(transactions: List<Transaction>): List<CategorySpending> {
        return transactions
            .filter { it.type == TransactionType.DEBIT && it.category != null }
            .groupBy { it.category!! }
            .map { (category, txns) ->
                CategorySpending(category, txns.sumOf { it.amount })
            }
            .sortedByDescending { it.amount }
    }

    //   Works on all Android versions
    private fun calculateDailySpending(transactions: List<Transaction>, timeFrame: TimeFrame): List<DailySpending> {
        val days = when (timeFrame) {
            TimeFrame.WEEKLY -> 7
            TimeFrame.MONTHLY -> 30
            TimeFrame.YEARLY -> 365
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return transactions
            .filter { it.type == TransactionType.DEBIT }
            .groupBy {
                //  Works on all Android versions
                dateFormat.format(Date(it.timestamp))
            }
            .map { (date, txns) ->
                DailySpending(date, txns.sumOf { it.amount })
            }
            .sortedBy { it.date }
            .takeLast(days)
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        private const val TAG = "InsightsViewModel"
    }
}
