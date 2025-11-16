package com.example.data.local


import com.example.data.remote.LocationManager
import com.example.domain.repository.LocationProvider
import javax.inject.Inject

class LocationProviderImpl @Inject constructor(
    private val locationManager: LocationManager
) : LocationProvider {

    override suspend fun getCurrentLocation(): LocationProvider.LocationData? {
        val result = locationManager.getCurrentLocation() ?: return null

        return LocationProvider.LocationData(
            latitude = result.latitude,
            longitude = result.longitude,
            accuracy = result.accuracy,
            timestamp = result.timestamp
        )
    }
}
