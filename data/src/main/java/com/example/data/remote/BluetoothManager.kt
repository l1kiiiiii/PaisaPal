package com.example.data.remote

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager as SystemBluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.domain.model.BluetoothFingerprint
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? SystemBluetoothManager
        manager?.adapter
    }

    private val soundBoxPrefixes = listOf(
        "Paytm_Box",
        "PhonePe_Box",
        "BharatPe",
        "Jio_Soundbox"
    )

    private val rssiThreshold = -75 // dBm
    private val signalDeltaThreshold = 5 // dBm

    suspend fun scanForSoundBoxes(): BluetoothFingerprint? {
        if (!hasBluetoothPermissions()) return null
        if (bluetoothAdapter?.isEnabled != true) return null

        val devices = withTimeoutOrNull(2000L) {
            performScan()
        } ?: emptyList()

        return selectBestDevice(devices)
    }

    private suspend fun performScan(): List<BluetoothFingerprint> =
        suspendCancellableCoroutine { continuation ->
            val devices = mutableListOf<BluetoothFingerprint>()
            val scanner = bluetoothAdapter?.bluetoothLeScanner

            if (scanner == null) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result?.let {
                        val deviceName = it.device?.name ?: return
                        val macAddress = it.device?.address ?: return
                        val rssi = it.rssi

                        if (isSoundBox(deviceName) && rssi > rssiThreshold) {
                            devices.add(
                                BluetoothFingerprint(
                                    macAddress = macAddress,
                                    deviceName = deviceName,
                                    rssi = rssi
                                )
                            )
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    if (continuation.isActive) {
                        continuation.resume(emptyList())
                    }
                }
            }

            try {
                scanner.startScan(callback)
            } catch (e: SecurityException) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation {
                try {
                    scanner.stopScan(callback)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // Stop scan after delay using coroutine context
            CoroutineScope(continuation.context).launch {
                delay(2000L)
                try {
                    scanner.stopScan(callback)
                } catch (e: Exception) {
                    // Ignore
                }
                if (continuation.isActive) {
                    continuation.resume(devices)
                }
            }
        }

    private fun isSoundBox(deviceName: String): Boolean {
        return soundBoxPrefixes.any { deviceName.contains(it, ignoreCase = true) }
    }

    private fun selectBestDevice(devices: List<BluetoothFingerprint>): BluetoothFingerprint? {
        if (devices.isEmpty()) return null

        val sorted = devices.sortedByDescending { it.rssi }

        // Return strongest device if only one or clear winner
        if (sorted.size == 1) return sorted.first()

        val strongest = sorted[0]
        val secondStrongest = sorted[1]

        // If signal delta is small, we're in a cluster (handle in strategy layer)
        // For now, return the strongest
        return strongest
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
