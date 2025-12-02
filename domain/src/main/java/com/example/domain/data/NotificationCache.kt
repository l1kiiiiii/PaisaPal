package com.example.domain.data

data class PaymentNotification(
    val amount: Double,
    val merchantName: String?,
    val packageName: String,
    val appName: String,
    val fullText: String,
    val timestamp: Long
)

interface NotificationCache {
    fun addNotification(
        amount: Double,
        merchantName: String?,
        packageName: String,
        appName: String,
        fullText: String,
        timestamp: Long = System.currentTimeMillis()
    )

    fun findMatchingNotification(
        amount: Double,
        timestamp: Long,
        timeWindowMs: Long = 2 * 60 * 1000L
    ): PaymentNotification?

    fun getRecentNotifications(limit: Int = 50): List<PaymentNotification>

    fun clear()
}
