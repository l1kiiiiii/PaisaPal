
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val type: String,
    val merchantRaw: String?,
    val merchantDisplayName: String?,
    val category: String?,
    val timestamp: Long,
    val smsBody: String,
    val sender: String,
    val referenceNumber: String?,
    val upiVpa: String?,
    val needsReview: Boolean,

    // Context fields
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationAccuracy: Float? = null,
    val contextSource: String? = null,
    val detectedPlace: String? = null,
    val contextConfidence: Float? = null
)
