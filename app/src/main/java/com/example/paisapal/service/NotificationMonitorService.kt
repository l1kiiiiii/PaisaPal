package com.example.paisapal.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.domain.model.NotificationData
import com.example.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationMonitorService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "NotificationMonitor"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        serviceScope.launch {
            try {
                val notification = sbn.notification ?: return@launch
                val packageName = sbn.packageName

                // Only process known apps
                if (!AppRegistry.isKnownApp(packageName)) return@launch

                // Extract transaction details from notification
                val notificationData = extractTransactionData(notification, packageName, sbn)

                if (notificationData != null) {
                    Log.d(TAG, "Captured notification: $notificationData")
                    notificationRepository.addNotification(notificationData)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    private fun extractTransactionData(
        notification: Notification,
        packageName: String,
        sbn: StatusBarNotification
    ): NotificationData? {
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val fullText = "$title $text"

        // Extract amount from notification text
        val amount = extractAmount(fullText) ?: return null

        // Get app metadata
        val appInfo = AppRegistry.getAppInfo(packageName) ?: return null

        return NotificationData(
            packageName = packageName,
            appName = appInfo.displayName,
            amount = amount,
            timestamp = sbn.postTime,
            fullText = fullText,
            suggestedCategory = appInfo.category,
            merchantName = appInfo.displayName
        )
    }

    private fun extractAmount(text: String): Double? {
        // Match patterns like: ₹500, Rs.500, INR 500, 500.00
        val patterns = listOf(
            "₹\\s*([0-9,]+(?:\\.[0-9]{2})?)",
            "rs\\.?\\s*([0-9,]+(?:\\.[0-9]{2})?)",
            "inr\\s*([0-9,]+(?:\\.[0-9]{2})?)",
            "\\b([0-9]{2,6}(?:\\.[0-9]{2})?)\\b" // Fallback: just numbers
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return amountStr.toDoubleOrNull()
            }
        }
        return null
    }
}
