// app/src/main/java/com/example/paisapal/service/NotificationMonitorService.kt
package com.example.paisapal.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.domain.data.AppRegistry  // ADD THIS
import com.example.domain.data.NotificationCache  // ADD THIS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationMonitorService : NotificationListenerService() {

    @Inject
    lateinit var notificationCache: NotificationCache

    companion object {
        private const val TAG = "NotificationMonitor"

        // Keywords that indicate a payment notification
        private val PAYMENT_KEYWORDS = listOf(
            "paid", "payment", "sent", "transferred", "debited",
            "₹", "rs", "rs.", "inr"
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName

            // Only process known payment apps
            if (!AppRegistry.isKnownPaymentApp(packageName)) {
                return
            }

            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

            val fullText = "$title $text $bigText".lowercase()

            // Check if it's a payment notification
            if (!isPaymentNotification(fullText)) {
                return
            }

            // Extract payment details
            val amount = extractAmount(fullText)
            val merchantName = extractMerchantName(fullText)

            if (amount != null && amount > 0) {
                notificationCache.addNotification(
                    amount = amount,
                    merchantName = merchantName,
                    packageName = packageName,
                    timestamp = sbn.postTime
                )

                Log.d(TAG, "Payment notification detected: ₹$amount to $merchantName via $packageName")

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        applicationContext,
                        "PaisaPal: ₹$amount detected",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    private fun isPaymentNotification(text: String): Boolean {
        return PAYMENT_KEYWORDS.any { keyword -> text.contains(keyword) }
    }

    private fun extractAmount(text: String): Double? {
        // Pattern for amount: ₹123.45 or Rs 123 or Rs. 123.45
        val patterns = listOf(
            "₹\\s*(\\d+(?:\\.\\d{2})?)",
            "rs\\.?\\s*(\\d+(?:\\.\\d{2})?)",
            "inr\\s*(\\d+(?:\\.\\d{2})?)"
        )

        patterns.forEach { pattern ->
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }

        return null
    }

    private fun extractMerchantName(text: String): String? {
        // Common patterns in payment notifications
        val patterns = listOf(
            "to\\s+([^₹\\n]+?)(?:\\s+₹|\\n|\$)",
            "paid\\s+([^₹\\n]+?)(?:\\s+₹|\\n|\$)",
            "sent to\\s+([^₹\\n]+?)(?:\\s+₹|\\n|\$)"
        )

        patterns.forEach { pattern ->
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)
            if (match != null) {
                val name = match.groupValues[1].trim()
                if (name.isNotBlank() && name.length > 2) {
                    return name
                }
            }
        }

        return null
    }
}
