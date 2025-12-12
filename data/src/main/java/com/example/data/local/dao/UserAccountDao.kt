package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity)

    @Delete
    suspend fun deleteAccount(account: UserAccountEntity)

    @Query("SELECT * FROM user_accounts ORDER BY addedAt DESC")
    suspend fun getAllAccounts(): List<UserAccountEntity>

    @Query("SELECT * FROM user_accounts ORDER BY addedAt DESC")
    fun observeAccounts(): Flow<List<UserAccountEntity>>

    @Query("DELETE FROM user_accounts WHERE last4Digits = :last4Digits")
    suspend fun deleteByDigits(last4Digits: String)
}
