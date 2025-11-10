package com.example.domain.repository

import com.example.domain.model.NotificationData

interface NotificationRepository {
    suspend fun addNotification(data: NotificationData)
    suspend fun getRecentNotifications(startTime: Long, endTime: Long): List<NotificationData>
    suspend fun findByAmount(amount: Double, timeWindow: Long): NotificationData?
    suspend fun clearCache()
}
