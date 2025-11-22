package com.example.data.system

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentLinkedQueue

data class CachedNotification(
    val packageName: String,
    val title: String?,
    val text: String?,
    val timestamp: Long
)

class NotificationCache : NotificationListenerService() {

    companion object {
        private val notificationQueue = ConcurrentLinkedQueue<CachedNotification>()
        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected

        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        private var cleanupJob: Job? = null

        fun getRecentNotifications(
            aroundTimestamp: Long,
            windowMs: Long = 60_000L
        ): List<CachedNotification> {
            cleanOldNotifications()

            val startTime = aroundTimestamp - windowMs
            val endTime = aroundTimestamp + windowMs

            return notificationQueue.filter {
                it.timestamp in startTime..endTime
            }
        }

        fun getNotificationByPackage(
            packageName: String,
            aroundTimestamp: Long,
            windowMs: Long = 60_000L
        ): CachedNotification? {
            return getRecentNotifications(aroundTimestamp, windowMs)
                .firstOrNull { it.packageName == packageName }
        }

        private fun cleanOldNotifications() {
            val cutoffTime = System.currentTimeMillis() - CACHE_DURATION_MS
            notificationQueue.removeAll { it.timestamp < cutoffTime }
        }

        private fun startPeriodicCleanup(scope: CoroutineScope) {
            cleanupJob?.cancel()
            cleanupJob = scope.launch {
                while (isActive) {
                    delay(5 * 60 * 1000L) // Every 5 minutes
                    cleanOldNotifications()
                }
            }
        }

        private fun stopPeriodicCleanup() {
            cleanupJob?.cancel()
            cleanupJob = null
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceConnected.value = true
        startPeriodicCleanup(serviceScope)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceConnected.value = false
        stopPeriodicCleanup()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val notification = it.notification
            val extras = notification.extras

            val cachedNotification = CachedNotification(
                packageName = it.packageName,
                title = extras.getCharSequence("android.title")?.toString(),
                text = extras.getCharSequence("android.text")?.toString(),
                timestamp = it.postTime
            )

            notificationQueue.add(cachedNotification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
