package com.example.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val merchantRaw: String?,
    val merchantDisplayName: String?,
    val category: String?,
    val timestamp: Long,
    val smsBody: String,
    val sender: String,
    val referenceNumber: String?,
    val upiVpa: String?,
    val needsReview: Boolean,

    // ADD THESE NEW FIELDS
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationAccuracy: Float? = null,
    val contextSource: String? = null,
    val detectedPlace: String? = null,
    val contextConfidence: Float? = null,
    val accountLast4Digits: String? = null,
    val accountName: String? = null
)

enum class TransactionType {
    DEBIT,
    CREDIT
}
