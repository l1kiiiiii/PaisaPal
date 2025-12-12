package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val last4Digits: String,
    val accountName: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
