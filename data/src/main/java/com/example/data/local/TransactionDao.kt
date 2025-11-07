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

    @Query("SELECT * FROM transactions WHERE referenceNumber = :refNo LIMIT 1")
    suspend fun findByReferenceNumber(refNo: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}
