
package com.example.domain.engine

import com.example.domain.data.AppRegistry
import com.example.domain.data.NotificationCache
import com.example.domain.model.SavedPlace
import com.example.domain.model.Transaction
import com.example.domain.repository.LocationProvider
import com.example.domain.repository.SavedPlaceRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.*

class ContextEngine @Inject constructor(
    private val notificationCache: NotificationCache,
    private val savedPlaceRepository: SavedPlaceRepository,
    private val locationProvider: LocationProvider
) {

    companion object {
        private const val TIME_WINDOW_MS = 2 * 60 * 1000L
        private const val PROXIMITY_THRESHOLD_METERS = 100.0
    }

    suspend fun enrichWithContext(transaction: Transaction): EnrichedTransaction {
        // Strategy 1: Notification-based context
        val notificationMatch = checkNotificationContext(transaction)
        if (notificationMatch != null) {
            return EnrichedTransaction(
                transaction = transaction.copy(
                    merchantDisplayName = notificationMatch.merchantName,
                    category = notificationMatch.category,
                    contextSource = "notification",
                    contextConfidence = notificationMatch.confidence,
                    needsReview = false
                ),
                contextMatch = notificationMatch
            )
        }

        // Strategy 2: Location-based context
        val locationMatch = checkLocationContext(transaction)
        if (locationMatch != null) {
            return EnrichedTransaction(
                transaction = transaction.copy(
                    merchantDisplayName = locationMatch.placeName,
                    category = locationMatch.category,
                    detectedPlace = locationMatch.placeName,
                    locationLat = locationMatch.latitude,
                    locationLng = locationMatch.longitude,
                    locationAccuracy = locationMatch.accuracy,
                    contextSource = "location",
                    contextConfidence = locationMatch.confidence,
                    needsReview = false
                ),
                contextMatch = locationMatch
            )
        }

        // No context found - save location anyway
        val currentLocation = locationProvider.getCurrentLocation()

        return EnrichedTransaction(
            transaction = transaction.copy(
                locationLat = currentLocation?.latitude,
                locationLng = currentLocation?.longitude,
                locationAccuracy = currentLocation?.accuracy,
                contextSource = "none",
                needsReview = true
            ),
            contextMatch = null
        )
    }

    private fun checkNotificationContext(transaction: Transaction): ContextMatch.NotificationBased? {
        val notification = notificationCache.findMatchingNotification(
            amount = transaction.amount,
            timestamp = transaction.timestamp,
            timeWindowMs = TIME_WINDOW_MS
        ) ?: return null

        val appInfo = AppRegistry.getAppInfo(notification.packageName) ?: return null
        val merchantName = notification.merchantName ?: appInfo.displayName

        return ContextMatch.NotificationBased(
            merchantName = merchantName,
            category = appInfo.category,
            appName = appInfo.displayName,
            packageName = notification.packageName,
            confidence = 0.9f
        )
    }

    private suspend fun checkLocationContext(transaction: Transaction): ContextMatch.LocationBased? {
        val currentLocation = locationProvider.getCurrentLocation() ?: return null

        val timeDiff = System.currentTimeMillis() - transaction.timestamp
        if (timeDiff > TIME_WINDOW_MS) {
            return null
        }

        // FIXED: Collect Flow to List
        val savedPlaces: List<SavedPlace> = savedPlaceRepository.getAllPlaces().firstOrNull() ?: emptyList()

        for (place in savedPlaces) {
            val distance = calculateDistance(
                currentLocation.latitude,
                currentLocation.longitude,
                place.latitude,
                place.longitude
            )

            if (distance <= PROXIMITY_THRESHOLD_METERS) {
                return ContextMatch.LocationBased(
                    placeName = place.name,
                    category = place.category ?: "Shopping",
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    accuracy = currentLocation.accuracy,
                    distance = distance,
                    confidence = calculateLocationConfidence(distance, currentLocation.accuracy)
                )
            }
        }

        return null
    }

    private fun calculateLocationConfidence(distance: Double, accuracy: Float): Float {
        val distanceScore = (1 - (distance / PROXIMITY_THRESHOLD_METERS)).coerceIn(0.0, 1.0)
        val accuracyScore = (1 - (accuracy.toDouble() / 50.0)).coerceIn(0.0, 1.0)
        return ((distanceScore * 0.7 + accuracyScore * 0.3).toFloat()).coerceIn(0.5f, 0.95f)
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    data class EnrichedTransaction(
        val transaction: Transaction,
        val contextMatch: ContextMatch?
    )

    sealed class ContextMatch {
        abstract val merchantName: String
        abstract val category: String
        abstract val confidence: Float

        data class NotificationBased(
            override val merchantName: String,
            override val category: String,
            val appName: String,
            val packageName: String,
            override val confidence: Float
        ) : ContextMatch()

        data class LocationBased(
            val placeName: String,
            override val merchantName: String = placeName,
            override val category: String,
            val latitude: Double,
            val longitude: Double,
            val accuracy: Float,
            val distance: Double,
            override val confidence: Float
        ) : ContextMatch()
    }
}
