package com.example.domain.engine

import com.example.domain.model.NotificationData
import com.example.domain.model.Transaction
import com.example.domain.repository.NotificationRepository
import javax.inject.Inject
import kotlin.math.abs

class ContextEngine @Inject constructor(
    private val notificationRepository: NotificationRepository
) {

    companion object {
        private const val TIME_WINDOW_MS = 2 * 60 * 1000L // 2 minutes
        private const val AMOUNT_TOLERANCE = 0.01
    }

    suspend fun enrichWithContext(transaction: Transaction): ContextMatch? {
        // Check notification cache for matching transaction
        val matchingNotification = notificationRepository.findByAmount(
            amount = transaction.amount,
            timeWindow = TIME_WINDOW_MS
        )

        return matchingNotification?.let { notification ->
            ContextMatch.Notification(
                appName = notification.appName,
                category = notification.suggestedCategory,
                displayName = notification.merchantName,
                packageName = notification.packageName,
                confidence = calculateConfidence(transaction, notification)
            )
        }
    }

    private fun calculateConfidence(
        transaction: Transaction,
        notification: NotificationData
    ): Float {
        var confidence = 0.5f

        // Amount match is primary
        if (abs(transaction.amount - notification.amount) < AMOUNT_TOLERANCE) {
            confidence += 0.3f
        }

        // Time proximity
        val timeDiff = abs(transaction.timestamp - notification.timestamp)
        if (timeDiff < 30_000L) { // Within 30 seconds
            confidence += 0.2f
        } else if (timeDiff < 60_000L) { // Within 1 minute
            confidence += 0.1f
        }

        return confidence.coerceIn(0f, 1f)
    }

    sealed class ContextMatch {
        abstract val displayName: String
        abstract val category: String
        abstract val confidence: Float

        data class Notification(
            val appName: String,
            val packageName: String,
            override val category: String,
            override val displayName: String,
            override val confidence: Float
        ) : ContextMatch()
    }
}
