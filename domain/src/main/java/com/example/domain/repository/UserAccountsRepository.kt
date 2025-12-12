package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserAccountsRepository {
    suspend fun addAccount(last4Digits: String, accountName: String? = null)
    suspend fun removeAccount(last4Digits: String)
    suspend fun getAllAccounts(): List<UserAccount>
    fun observeAccounts(): Flow<List<UserAccount>>
}

data class UserAccount(
    val last4Digits: String,
    val accountName: String? = null,  // e.g., "HDFC Salary", "ICICI Savings"
    val addedAt: Long = System.currentTimeMillis()
)
