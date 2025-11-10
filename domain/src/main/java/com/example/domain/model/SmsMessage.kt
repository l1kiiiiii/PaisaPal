package com.example.domain.model


data class SmsMessage(
    val id: String,
    val address: String,
    val body: String,
    val timestamp: Long,
    val type: Int
)
