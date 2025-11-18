package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_corrections")
data class UserCorrectionEntity(
    @PrimaryKey
    val merchantName: String,
    val category: String,
    val timestamp: Long
)
