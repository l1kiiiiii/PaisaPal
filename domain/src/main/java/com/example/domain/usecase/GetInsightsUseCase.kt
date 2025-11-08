package com.example.domain.usecase

import com.example.domain.model.*
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class GetInsightsUseCase(
    private val repository: TransactionRepository
) {

    suspend fun execute(period: InsightsPeriod): InsightsData {
        val transactions = repository.getAllTransactions().first()
        val filteredTransactions = filterByPeriod(transactions, period)

        return InsightsData(
            totalSpent = calculateTotalSpent(filteredTransactions),
            totalIncome = calculateTotalIncome(filteredTransactions),
            categoryBreakdown = calculateCategoryBreakdown(filteredTransactions),
            monthlyTrend = calculateMonthlyTrend(filteredTransactions),
            topMerchants = calculateTopMerchants(filteredTransactions)
        )
    }

    private fun filterByPeriod(transactions: List<Transaction>, period: InsightsPeriod): List<Transaction> {
        val now = System.currentTimeMillis()
        val cutoffTime = when (period) {
            InsightsPeriod.DAILY -> now - (24 * 60 * 60 * 1000)
            InsightsPeriod.WEEKLY -> now - (7 * 24 * 60 * 60 * 1000)
            InsightsPeriod.MONTHLY -> now - (30L * 24 * 60 * 60 * 1000)
        }

        return transactions.filter { it.timestamp >= cutoffTime }
    }

    private fun calculateTotalSpent(transactions: List<Transaction>): Double {
        return transactions
            .filter { it.type == TransactionType.DEBIT }
            .sumOf { it.amount }
    }

    private fun calculateTotalIncome(transactions: List<Transaction>): Double {
        return transactions
            .filter { it.type == TransactionType.CREDIT }
            .sumOf { it.amount }
    }

    private fun calculateCategoryBreakdown(transactions: List<Transaction>): List<CategorySpending> {
        val debitTransactions = transactions.filter { it.type == TransactionType.DEBIT }
        val totalSpent = debitTransactions.sumOf { it.amount }

        val grouped = debitTransactions
            .filter { it.category != null }
            .groupBy { it.category!! }

        return grouped.map { (category, txns) ->
            val amount = txns.sumOf { it.amount }
            CategorySpending(
                category = category,
                amount = amount,
                percentage = if (totalSpent > 0) ((amount / totalSpent) * 100).toFloat() else 0f,
                transactionCount = txns.size
            )
        }.sortedByDescending { it.amount }
    }

    private fun calculateMonthlyTrend(transactions: List<Transaction>): List<MonthlySpending> {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Get last 6 months
        val monthlyData = mutableMapOf<String, Pair<Double, Long>>()

        for (i in 5 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            val monthKey = monthFormat.format(calendar.time)
            monthlyData[monthKey] = Pair(0.0, calendar.timeInMillis)
        }

        // Calculate spending per month
        transactions.filter { it.type == TransactionType.DEBIT }.forEach { txn ->
            calendar.timeInMillis = txn.timestamp
            val monthKey = monthFormat.format(calendar.time)

            monthlyData[monthKey]?.let { (currentAmount, timestamp) ->
                monthlyData[monthKey] = Pair(currentAmount + txn.amount, timestamp)
            }
        }

        return monthlyData.map { (month, data) ->
            MonthlySpending(month, data.first, data.second)
        }.sortedBy { it.date }
    }

    private fun calculateTopMerchants(transactions: List<Transaction>): List<MerchantSpending> {
        return transactions
            .filter { it.type == TransactionType.DEBIT && it.merchantDisplayName != null }
            .groupBy { it.merchantDisplayName!! }
            .map { (merchant, txns) ->
                MerchantSpending(
                    merchantName = merchant,
                    amount = txns.sumOf { it.amount },
                    transactionCount = txns.size
                )
            }
            .sortedByDescending { it.amount }
            .take(5)
    }
}

enum class InsightsPeriod {
    DAILY, WEEKLY, MONTHLY
}
