package com.example.domain.model

data class ContextSnapshot(
    val transactionId: String,
    val rawSms: String,
    val timestamp: Long,
    val foregroundAppPackage: String?,
    val recentNotification: String?,
    val strongestBluetoothDevice: BluetoothFingerprint?,
    val location: GeoLocation?,
    val amount: Double,
    val upiVpa: String? = null
)
