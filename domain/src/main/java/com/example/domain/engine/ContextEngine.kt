package com.example.domain.engine

import com.example.domain.model.Transaction
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.SavedPlaceRepository

class ContextEngine(
    private val notificationRepository: NotificationRepository,
    private val savedPlaceRepository: SavedPlaceRepository
) {

    companion object {
        private const val TIME_WINDOW_MS = 2 * 60 * 1000L
    }

    suspend fun enrichWithContext(transaction: Transaction): ContextMatch? {
        val notificationMatch = checkNotificationContext(transaction)
        if (notificationMatch != null) return notificationMatch

        return null
    }

    private suspend fun checkNotificationContext(transaction: Transaction): ContextMatch? {
        val notification = notificationRepository.findByAmount(
            amount = transaction.amount,
            timeWindow = TIME_WINDOW_MS
        ) ?: return null

        return ContextMatch.NotificationBased(
            merchantName = notification.merchantName,
            category = notification.suggestedCategory,
            appName = notification.appName,
            confidence = 0.9f
        )
    }

    sealed class ContextMatch {
        abstract val merchantName: String
        abstract val category: String
        abstract val confidence: Float

        data class NotificationBased(
            override val merchantName: String,
            override val category: String,
            val appName: String,
            override val confidence: Float
        ) : ContextMatch()
    }
}
