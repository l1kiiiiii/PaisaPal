package com.example.data.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        private const val TAG = "LocationManager"
        private const val LOCATION_TIMEOUT_MS = 10000L // 10 seconds
    }

    /**
     * Get current location (if permission granted)
     * Returns null if:
     * - Permission not granted
     * - Location unavailable
     * - Timeout
     */
    suspend fun getCurrentLocation(): LocationResult? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            getCurrentLocationInternal()
        } ?: run {
            Log.w(TAG, "Location request timed out")
            null
        }
    }

    private suspend fun getCurrentLocationInternal(): LocationResult? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val cancellationToken = CancellationTokenSource()

                // Check permission again right before API call
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w(TAG, "Permission check failed at API call time")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val result = LocationResult(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                        Log.d(TAG, "Got location: (${result.latitude}, ${result.longitude}) ±${result.accuracy}m")
                        continuation.resume(result)
                    } else {
                        Log.w(TAG, "Location is null")
                        continuation.resume(null)
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get location", exception)
                    continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    cancellationToken.cancel()
                }

            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception getting location", e)
                continuation.resume(null)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting location", e)
                continuation.resume(null)
            }
        }
    }

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Long
    )
}
