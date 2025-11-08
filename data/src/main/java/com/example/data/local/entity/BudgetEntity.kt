package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val limitAmount: Double,
    val period: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val alertThreshold: Int,
    val isActive: Boolean,
    val createdAt: Long
)
