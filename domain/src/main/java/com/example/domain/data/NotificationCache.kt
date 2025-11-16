package com.example.domain.data

data class PaymentNotification(
    val amount: Double,
    val merchantName: String?,
    val packageName: String,
    val timestamp: Long
)

interface NotificationCache {
    fun addNotification(
        amount: Double,
        merchantName: String?,
        packageName: String,
        timestamp: Long = System.currentTimeMillis()
    )

    fun findMatchingNotification(
        amount: Double,
        timestamp: Long,
        timeWindowMs: Long = 2 * 60 * 1000L
    ): PaymentNotification?

    fun clear()
}
