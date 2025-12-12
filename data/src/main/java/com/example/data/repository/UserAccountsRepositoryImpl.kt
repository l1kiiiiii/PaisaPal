package com.example.data.repository

import com.example.data.local.dao.UserAccountDao
import com.example.data.local.entity.UserAccountEntity
import com.example.domain.repository.UserAccount
import com.example.domain.repository.UserAccountsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserAccountsRepositoryImpl @Inject constructor(
    private val userAccountDao: UserAccountDao
) : UserAccountsRepository {

    override suspend fun addAccount(last4Digits: String, accountName: String?) {
        userAccountDao.insertAccount(
            UserAccountEntity(
                last4Digits = last4Digits,
                accountName = accountName
            )
        )
    }

    override suspend fun removeAccount(last4Digits: String) {
        userAccountDao.deleteByDigits(last4Digits)
    }

    override suspend fun getAllAccounts(): List<UserAccount> {
        return userAccountDao.getAllAccounts().map { it.toDomain() }
    }

    override fun observeAccounts(): Flow<List<UserAccount>> {
        return userAccountDao.observeAccounts().map { list ->
            list.map { it.toDomain() }
        }
    }

    private fun UserAccountEntity.toDomain() = UserAccount(
        last4Digits = last4Digits,
        accountName = accountName,
        addedAt = addedAt
    )
}
