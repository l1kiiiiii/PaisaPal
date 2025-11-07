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
    val needsReview: Boolean
)

enum class TransactionType {
    DEBIT,
    CREDIT
}
