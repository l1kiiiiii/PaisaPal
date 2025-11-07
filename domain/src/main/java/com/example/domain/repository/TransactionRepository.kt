package com.example.domain.repository

import com.example.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getUncategorizedTransactions(): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun findByReferenceNumber(refNo: String): Transaction?
}
