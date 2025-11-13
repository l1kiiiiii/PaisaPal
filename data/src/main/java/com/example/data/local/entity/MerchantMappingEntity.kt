package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_mappings")
data class MerchantMappingEntity(
    @PrimaryKey
    val id: String,
    val merchantKeyword: String,
    val category: String,
    val userConfirmed: Boolean,
    val usageCount: Int,
    val lastUsed: Long,
    val createdAt: Long
)
