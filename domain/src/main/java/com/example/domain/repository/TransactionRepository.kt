package com.example.domain.repository

import com.example.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // Flow-based queries (reactive, continuous updates)
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getUncategorizedTransactions(): Flow<List<Transaction>>
    fun getTransactionByIdFlow(transactionId: String): Flow<Transaction?>

    // Suspend functions (one-time operations)
    suspend fun insert(transaction: Transaction)
    suspend fun insertMultiple(transactions: List<Transaction>)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)

    // Query functions
    suspend fun getTransactionById(transactionId: String): Transaction?
    suspend fun findByReferenceNumber(refNo: String): Transaction?
    suspend fun findByAmountAndTimeRange(
        amount: Double,
        startTime: Long,
        endTime: Long
    ): List<Transaction>
    suspend fun getAllTransactionsWithReferenceNumber(): List<Transaction>

    // Utility functions
    suspend fun transactionExists(transactionId: String): Boolean
    suspend fun updateTransactionCategory(transactionId: String, category: String)
}
