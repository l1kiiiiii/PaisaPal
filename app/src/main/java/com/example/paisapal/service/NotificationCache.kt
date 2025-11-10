package com.example.paisapal.service

import com.example.domain.model.NotificationData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class NotificationCache @Inject constructor() {

    private val cache = mutableListOf<NotificationData>()
    private val mutex = Mutex()

    companion object {
        private const val MAX_AGE_MS = 5 * 60 * 1000 // 5 minutes
        private const val MAX_CACHE_SIZE = 50
    }

    suspend fun add(data: NotificationData) {
        mutex.withLock {
            cache.add(0, data) // Add to front

            // Cleanup old entries
            val now = System.currentTimeMillis()
            cache.removeAll { now - it.timestamp > MAX_AGE_MS }

            // Limit cache size
            if (cache.size > MAX_CACHE_SIZE) {
                cache.subList(MAX_CACHE_SIZE, cache.size).clear()
            }
        }
    }

    suspend fun getRecent(
        startTime: Long,
        endTime: Long
    ): List<NotificationData> {
        return mutex.withLock {
            cache.filter { it.timestamp in startTime..endTime }
        }
    }

    suspend fun findByAmount(
        amount: Double,
        timeWindow: Long = 2 * 60 * 1000 // 2 minutes
    ): NotificationData? {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            cache.firstOrNull {
                kotlin.math.abs(it.amount - amount) < 0.01 &&
                        now - it.timestamp < timeWindow
            }
        }
    }

    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
}
