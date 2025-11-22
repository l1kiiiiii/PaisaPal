package com.example.data.remote

import android.annotation.SuppressLint
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
import java.security.MessageDigest
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

    data class ScanResult(
        val device: BluetoothFingerprint?,
        val cluster: String? = null,
        val clusterDevices: List<BluetoothFingerprint>? = null
    )

    suspend fun scanForSoundBoxes(): ScanResult {
        if (!hasBluetoothPermissions()) return ScanResult(null)
        if (bluetoothAdapter?.isEnabled != true) return ScanResult(null)

        val devices = withTimeoutOrNull(2000L) {
            performScan()
        } ?: emptyList()

        return selectBestDeviceOrCluster(devices)
    }

    @SuppressLint("MissingPermission")
    private suspend fun performScan(): List<BluetoothFingerprint> =
        suspendCancellableCoroutine { continuation ->
            val devices = mutableListOf<BluetoothFingerprint>()
            val scanner = bluetoothAdapter?.bluetoothLeScanner

            if (scanner == null) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            val callback = object : android.bluetooth.le.ScanCallback() {
                override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
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

    private fun selectBestDeviceOrCluster(devices: List<BluetoothFingerprint>): ScanResult {
        if (devices.isEmpty()) return ScanResult(null)

        val sorted = devices.sortedByDescending { it.rssi }

        if (sorted.size == 1) return ScanResult(sorted.first())

        val strongest = sorted[0]
        val secondStrongest = sorted[1]
        val rssiDelta = strongest.rssi - secondStrongest.rssi

        // Check if we're in a cluster
        if (rssiDelta < signalDeltaThreshold) {
            // Multiple devices with similar signal strength - create cluster
            val clusterDevices = sorted.filter {
                strongest.rssi - it.rssi < signalDeltaThreshold
            }
            val clusterHash = createClusterHash(clusterDevices)

            return ScanResult(
                device = strongest,
                cluster = clusterHash,
                clusterDevices = clusterDevices
            )
        }

        // Clear winner - single device
        return ScanResult(strongest)
    }

    private fun createClusterHash(devices: List<BluetoothFingerprint>): String {
        val sortedMacs = devices.map { it.macAddress }.sorted().joinToString("|")
        return hashString(sortedMacs).take(16)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun isSoundBox(deviceName: String): Boolean {
        return soundBoxPrefixes.any { deviceName.contains(it, ignoreCase = true) }
    }

    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
