package com.example.domain.repository


interface LocationProvider {
    suspend fun getCurrentLocation(): LocationData?

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Long
    )
}
