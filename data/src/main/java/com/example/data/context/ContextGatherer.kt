package com.example.data.context

import android.content.Context
import com.example.data.remote.BluetoothManager
import com.example.data.system.AppUsageTracker
import com.example.data.system.LocationProvider
import com.example.data.system.NotificationCache
import com.example.domain.model.ContextSnapshot
import kotlinx.coroutines.*

class ContextGatherer(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val appUsageTracker: AppUsageTracker,
    private val locationProvider: LocationProvider
) {

    suspend fun gatherContext(
        transactionId: String,
        rawSms: String,
        timestamp: Long,
        amount: Double,
        upiVpa: String?
    ): ContextSnapshot = withContext(Dispatchers.IO) {

        // Launch all context gathering operations in parallel with 2-second timeout
        val bluetoothDeferred = async {
            withTimeoutOrNull(2000L) {
                bluetoothManager.scanForSoundBoxes()
            }
        }

        val appDeferred = async {
            withTimeoutOrNull(500L) {
                appUsageTracker.getForegroundAppAtTime(timestamp)
            }
        }

        val notificationDeferred = async {
            withTimeoutOrNull(500L) {
                NotificationCache.getRecentNotifications(timestamp, 60_000L)
                    .firstOrNull()?.text
            }
        }

        val locationDeferred = async {
            withTimeoutOrNull(1000L) {
                locationProvider.getCurrentLocation()
            }
        }

        // Wait for all with overall timeout
        withTimeoutOrNull(2500L) {
            ContextSnapshot(
                transactionId = transactionId,
                rawSms = rawSms,
                timestamp = timestamp,
                foregroundAppPackage = appDeferred.await(),
                recentNotification = notificationDeferred.await(),
                strongestBluetoothDevice = bluetoothDeferred.await(),
                location = locationDeferred.await(),
                amount = amount,
                upiVpa = upiVpa
            )
        } ?: ContextSnapshot(
            transactionId = transactionId,
            rawSms = rawSms,
            timestamp = timestamp,
            foregroundAppPackage = null,
            recentNotification = null,
            strongestBluetoothDevice = null,
            location = null,
            amount = amount,
            upiVpa = upiVpa
        )
    }
}
