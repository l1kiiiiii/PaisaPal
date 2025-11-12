package com.example.data.local

import androidx.room.*
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE needsReview = 1 ORDER BY timestamp DESC")
    fun getUncategorizedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun getTransactionByIdFlow(transactionId: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE referenceNumber = :refNo LIMIT 1")
    suspend fun findByReferenceNumber(refNo: String): TransactionEntity?

    @Query("""
        SELECT * FROM transactions
        WHERE amount = :amount
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun findByAmountAndTimeRange(
        amount: Double,
        startTime: Long,
        endTime: Long
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE referenceNumber IS NOT NULL")
    suspend fun getAllTransactionsWithReferenceNumber(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("UPDATE transactions SET category = :category, needsReview = 0 WHERE id = :transactionId")
    suspend fun updateCategory(transactionId: String, category: String)
}
