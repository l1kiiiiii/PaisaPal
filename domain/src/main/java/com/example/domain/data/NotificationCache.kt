package com.example.domain.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class NotificationCache @Inject constructor() {

    private val _cachedNotifications = MutableStateFlow<List<PaymentNotification>>(emptyList())
    val cachedNotifications: StateFlow<List<PaymentNotification>> = _cachedNotifications.asStateFlow()

    companion object {
        private const val CACHE_RETENTION_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_SIZE = 50
    }

    /**
     * Add a payment notification to the cache
     */
    fun addNotification(
        amount: Double,
        merchantName: String?,
        packageName: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val notification = PaymentNotification(
            amount = amount,
            merchantName = merchantName,
            packageName = packageName,
            timestamp = timestamp
        )

        synchronized(_cachedNotifications) {
            val currentList = _cachedNotifications.value.toMutableList()
            currentList.add(notification)

            // Remove old notifications
            currentList.removeAll {
                System.currentTimeMillis() - it.timestamp > CACHE_RETENTION_MS
            }

            // Limit cache size
            if (currentList.size > MAX_CACHE_SIZE) {
                currentList.removeAt(0)
            }

            _cachedNotifications.value = currentList
            // Removed Log.d - domain module can't use Android Log
        }
    }

    /**
     * Find matching notification for a transaction
     * Matches by amount and time window
     */
    fun findMatchingNotification(
        amount: Double,
        timestamp: Long,
        timeWindowMs: Long = 2 * 60 * 1000L // 2 minutes
    ): PaymentNotification? {
        synchronized(_cachedNotifications) {
            return _cachedNotifications.value
                .filter { notification ->
                    // Match amount (allow 1 rupee difference for rounding)
                    val amountMatches = abs(notification.amount - amount) <= 1.0

                    // Match time window
                    val timeMatches = abs(notification.timestamp - timestamp) <= timeWindowMs

                    amountMatches && timeMatches
                }
                .minByOrNull { abs(it.timestamp - timestamp) }
        }
    }

    /**
     * Clear all cached notifications
     */
    fun clear() {
        _cachedNotifications.value = emptyList()
    }
}

data class PaymentNotification(
    val amount: Double,
    val merchantName: String?,
    val packageName: String,
    val timestamp: Long
)
