package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "context_signatures")
data class ContextSignatureEntity(
    @PrimaryKey val id: String,
    val triggerType: String,
    val triggerValue: String,
    val learnedCategory: String,
    val confidenceScore: Float,
    val hitCount: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)
