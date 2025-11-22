package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorSettings(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "sensor_settings",
        Context.MODE_PRIVATE
    )

    private val _bluetoothEnabled = MutableStateFlow(isBluetoothEnabled())
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled

    private val _locationEnabled = MutableStateFlow(isLocationEnabled())
    val locationEnabled: StateFlow<Boolean> = _locationEnabled

    private val _appUsageEnabled = MutableStateFlow(isAppUsageEnabled())
    val appUsageEnabled: StateFlow<Boolean> = _appUsageEnabled

    private val _notificationEnabled = MutableStateFlow(isNotificationEnabled())
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled

    fun isBluetoothEnabled(): Boolean {
        return prefs.getBoolean(KEY_BLUETOOTH, true)
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLUETOOTH, enabled).apply()
        _bluetoothEnabled.value = enabled
    }

    fun isLocationEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOCATION, true)
    }

    fun setLocationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION, enabled).apply()
        _locationEnabled.value = enabled
    }

    fun isAppUsageEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_USAGE, true)
    }

    fun setAppUsageEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_USAGE, enabled).apply()
        _appUsageEnabled.value = enabled
    }

    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION, true)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION, enabled).apply()
        _notificationEnabled.value = enabled
    }

    companion object {
        private const val KEY_BLUETOOTH = "sensor_bluetooth"
        private const val KEY_LOCATION = "sensor_location"
        private const val KEY_APP_USAGE = "sensor_app_usage"
        private const val KEY_NOTIFICATION = "sensor_notification"
    }
}
