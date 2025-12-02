package com.example.data.context

import android.content.Context
import com.example.data.permission.PermissionManager
import com.example.data.remote.BluetoothManager
import com.example.data.settings.SensorSettings
import com.example.data.system.AppUsageTracker
import com.example.data.system.LocationProvider
import com.example.domain.data.NotificationCache  //  Now an interface
import com.example.domain.model.ContextSnapshot
import kotlinx.coroutines.*
import javax.inject.Inject

class ContextGatherer @Inject constructor(  //  Added @Inject for Hilt
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val appUsageTracker: AppUsageTracker,
    private val locationProvider: LocationProvider,
    private val permissionManager: PermissionManager,
    private val sensorSettings: SensorSettings,
    private val notificationCache: NotificationCache  //  Inject the interface
) {

    suspend fun gatherContext(
        transactionId: String,
        rawSms: String,
        timestamp: Long,
        amount: Double,
        upiVpa: String?
    ): ContextSnapshot = withContext(Dispatchers.IO) {

        // Check permissions and settings
        val permissions = permissionManager.checkAllPermissions()

        // Launch context gathering with graceful degradation
        val bluetoothDeferred = async {
            if (permissions.bluetooth && sensorSettings.isBluetoothEnabled()) {
                withTimeoutOrNull(2000L) {
                    bluetoothManager.scanForSoundBoxes().device
                }
            } else null
        }

        val appDeferred = async {
            if (permissions.usageStats && sensorSettings.isAppUsageEnabled()) {
                withTimeoutOrNull(500L) {
                    appUsageTracker.getForegroundAppAtTime(timestamp)
                }
            } else null
        }

        val notificationDeferred = async {
            if (permissions.notificationListener && sensorSettings.isNotificationEnabled()) {
                withTimeoutOrNull(500L) {
                    //   Use injected cache and correct field name
                    notificationCache.getRecentNotifications(50)
                        .firstOrNull {
                            kotlin.math.abs(it.timestamp - timestamp) <= 60_000L
                        }?.fullText  //  Use 'fullText' instead of 'text'
                }
            } else null
        }

        val locationDeferred = async {
            if (permissions.location && sensorSettings.isLocationEnabled()) {
                withTimeoutOrNull(1000L) {
                    locationProvider.getCurrentLocation()
                }
            } else null
        }

        // Hard timeout for all operations
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
