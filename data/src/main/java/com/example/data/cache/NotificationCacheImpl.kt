package com.example.data.cache

import com.example.domain.data.NotificationCache
import com.example.domain.data.PaymentNotification
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCacheImpl @Inject constructor() : NotificationCache {

    private val cache = mutableListOf<PaymentNotification>()
    private val maxSize = 50
    private val retentionMs = 5 * 60 * 1000L

    override fun addNotification(
        amount: Double,
        merchantName: String?,
        packageName: String,
        timestamp: Long
    ) {
        synchronized(cache) {
            cache.add(PaymentNotification(amount, merchantName, packageName, timestamp))
            cache.removeAll { System.currentTimeMillis() - it.timestamp > retentionMs }
            if (cache.size > maxSize) cache.removeAt(0)
        }
    }

    override fun findMatchingNotification(
        amount: Double,
        timestamp: Long,
        timeWindowMs: Long
    ): PaymentNotification? {
        synchronized(cache) {
            return cache
                .filter { kotlin.math.abs(it.amount - amount) <= 1.0 }
                .filter { kotlin.math.abs(it.timestamp - timestamp) <= timeWindowMs }
                .minByOrNull { kotlin.math.abs(it.timestamp - timestamp) }
        }
    }

    override fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }
}
