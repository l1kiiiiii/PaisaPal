package com.example.paisapal.ui.screens.insights

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class TimeFrame {
    WEEKLY, MONTHLY, YEARLY
}

data class CategorySpending(
    val category: String,
    val amount: Double
)

data class DailySpending(
    val date: LocalDate,
    val amount: Double
)

data class InsightsState(
    val timeFrame: TimeFrame = TimeFrame.MONTHLY,
    val categorySpending: List<CategorySpending> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val totalSpent: Double = 0.0
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _timeFrame = MutableStateFlow(TimeFrame.MONTHLY)

    private val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val insightsState: StateFlow<InsightsState> = combine(
        transactions,
        _timeFrame
    ) { txns, timeFrame ->
        val filtered = filterByTimeFrame(txns, timeFrame)
        InsightsState(
            timeFrame = timeFrame,
            categorySpending = calculateCategorySpending(filtered),
            dailySpending = calculateDailySpending(filtered, timeFrame),
            totalSpent = filtered.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
        )
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

    private fun calculateDailySpending(transactions: List<Transaction>, timeFrame: TimeFrame): List<DailySpending> {
        val days = when (timeFrame) {
            TimeFrame.WEEKLY -> 7
            TimeFrame.MONTHLY -> 30
            TimeFrame.YEARLY -> 365
        }

        return transactions
            .filter { it.type == TransactionType.DEBIT }
            .groupBy {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.ofEpochMilli(it.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            }
            .map { (date, txns) ->
                DailySpending(date, txns.sumOf { it.amount })
            }
            .sortedBy { it.date }
            .takeLast(days)
    }
}
