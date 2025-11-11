package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_cache")
data class NotificationCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val amount: Double,
    val timestamp: Long,
    val category: String,
    val merchantDisplayName: String,
    val fullText: String
)
