package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUncategorizedTransactions(): Flow<List<Transaction>> {
        return dao.getUncategorizedTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun insertMultiple(transactions: List<Transaction>) {
        dao.insertMultiple(transactions.map { it.toEntity() })
    }

    override suspend fun update(transaction: Transaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        dao.delete(transaction.toEntity())
    }

    override suspend fun findByReferenceNumber(refNo: String): Transaction? {
        return dao.findByReferenceNumber(refNo)?.toDomain()
    }

    override suspend fun findByAmountAndTimeRange(
        amount: Double,
        startTime: Long,
        endTime: Long
    ): List<Transaction> {
        return dao.findByAmountAndTimeRange(amount, startTime, endTime).map { it.toDomain() }
    }

    override suspend fun getAllTransactionsWithReferenceNumber(): List<Transaction> {
        return dao.getAllTransactionsWithReferenceNumber().map { it.toDomain() }
    }

    override suspend fun transactionExists(transactionId: String): Boolean {
        return dao.getTransactionById(transactionId) != null
    }

    override suspend fun updateTransactionCategory(transactionId: String, category: String) {
        dao.updateCategory(transactionId, category)
    }

    override suspend fun getTransactionById(transactionId: String): Transaction? {
        return dao.getTransactionById(transactionId)?.toDomain()
    }

    override fun getTransactionByIdFlow(transactionId: String): Flow<Transaction?> {
        return dao.getTransactionByIdFlow(transactionId).map { it?.toDomain() }
    }
}
