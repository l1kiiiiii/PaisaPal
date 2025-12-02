package com.example.data.cache

import com.example.domain.data.NotificationCache
import com.example.domain.data.PaymentNotification
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class NotificationCacheImpl @Inject constructor() : NotificationCache {

    private val cache = mutableListOf<PaymentNotification>()
    private val maxSize = 50
    private val retentionMs = 5 * 60 * 1000L

    override fun addNotification(
        amount: Double,
        merchantName: String?,
        packageName: String,
        appName: String,
        fullText: String,
        timestamp: Long
    ) {
        synchronized(cache) {
            cache.add(0, PaymentNotification(
                amount, merchantName, packageName, appName, fullText, timestamp
            ))

            val now = System.currentTimeMillis()
            cache.removeAll { now - it.timestamp > retentionMs }
            if (cache.size > maxSize) {
                cache.subList(maxSize, cache.size).clear()
            }
        }
    }

    override fun findMatchingNotification(
        amount: Double,
        timestamp: Long,
        timeWindowMs: Long
    ): PaymentNotification? {
        synchronized(cache) {
            return cache
                .filter { abs(it.amount - amount) <= 1.0 }
                .filter { abs(it.timestamp - timestamp) <= timeWindowMs }
                .minByOrNull { abs(it.timestamp - timestamp) }
        }
    }

    override fun getRecentNotifications(limit: Int): List<PaymentNotification> {
        synchronized(cache) {
            return cache.take(limit)
        }
    }

    override fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }
}
