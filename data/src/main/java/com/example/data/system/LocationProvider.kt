package com.example.data.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.domain.model.GeoLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    suspend fun getCurrentLocation(): GeoLocation? {
        if (!hasLocationPermission()) return null

        return getLastKnownLocation()
    }

    private fun getLastKnownLocation(): GeoLocation? {
        val providers = locationManager?.getProviders(true) ?: return null

        var bestLocation: Location? = null

        for (provider in providers) {
            try {
                val location = locationManager?.getLastKnownLocation(provider)
                if (location != null) {
                    if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                        bestLocation = location
                    }
                }
            } catch (e: SecurityException) {
                // Permission denied
                return null
            }
        }

        return bestLocation?.let {
            GeoLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                accuracy = it.accuracy,
                savedPlaceId = null // Will be matched in strategy layer
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
