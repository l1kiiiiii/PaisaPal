package com.example.domain.model

data class MerchantMapping(
    val id: String,
    val merchantKeyword: String,      // "ZOMATO", "SWIGGY", "AMAZON"
    val category: String,              // "Food & Dining", "Shopping"
    val userConfirmed: Boolean = true, // User-confirmed vs auto-suggested
    val usageCount: Int = 1,           // How many times this mapping was used
    val lastUsed: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
