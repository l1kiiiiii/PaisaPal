package com.example.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_mappings")
data class MerchantMappingEntity(
    @PrimaryKey val upiVpa: String,
    val merchantName: String,
    val category: String
)
