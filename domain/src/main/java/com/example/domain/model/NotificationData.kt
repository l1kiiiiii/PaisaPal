package com.example.domain.model

data class NotificationData(
    val packageName: String,
    val appName: String,
    val amount: Double,
    val timestamp: Long,
    val fullText: String,
    val suggestedCategory: String,
    val merchantName: String
)
