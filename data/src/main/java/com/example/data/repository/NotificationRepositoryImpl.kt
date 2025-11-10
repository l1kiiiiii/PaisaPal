package com.example.data.repository

import com.example.domain.model.NotificationData
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val cache = mutableListOf<NotificationData>()
    private val mutex = Mutex()

    companion object {
        private const val MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_SIZE = 50
    }

    override suspend fun addNotification(data: NotificationData) {
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

    override suspend fun getRecentNotifications(
        startTime: Long,
        endTime: Long
    ): List<NotificationData> {
        return mutex.withLock {
            cache.filter { it.timestamp in startTime..endTime }
        }
    }

    override suspend fun findByAmount(
        amount: Double,
        timeWindow: Long
    ): NotificationData? {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            cache.firstOrNull {
                abs(it.amount - amount) < 0.01 &&
                        now - it.timestamp < timeWindow
            }
        }
    }

    override suspend fun clearCache() {
        mutex.withLock {
            cache.clear()
        }
    }
}
