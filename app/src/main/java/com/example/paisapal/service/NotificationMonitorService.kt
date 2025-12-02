package com.example.paisapal.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.domain.data.AppRegistry
import com.example.domain.data.NotificationCache
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationMonitorService : NotificationListenerService() {

    @Inject
    lateinit var notificationCache: NotificationCache

    companion object {
        private const val TAG = "NotificationMonitor"
        private val PAYMENT_KEYWORDS = listOf(
            "paid", "payment", "sent", "transferred", "debited",
            "₹", "rs", "rs.", "inr"
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName

            if (!AppRegistry.isKnownPaymentApp(packageName)) return

            val notification = sbn.notification
            val extras = notification.extras
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""
            val fullText = "$title $text $bigText"

            if (!isPaymentNotification(fullText.lowercase())) return

            val amount = extractAmount(fullText.lowercase())
            val merchantName = extractMerchantName(fullText.lowercase())
            val appName = AppRegistry.getAppInfo(packageName)?.displayName ?: "Unknown App"

            if (amount != null && amount > 0) {
                notificationCache.addNotification(
                    amount = amount,
                    merchantName = merchantName,
                    packageName = packageName,
                    appName = appName,
                    fullText = fullText,
                    timestamp = sbn.postTime
                )

                Log.d(TAG, "Payment detected: ₹$amount to $merchantName via $appName")

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

    private fun isPaymentNotification(text: String) =
        PAYMENT_KEYWORDS.any { text.contains(it) }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            "₹\\s*(\\d+(?:\\.\\d{2})?)",
            "rs\\.?\\s*(\\d+(?:\\.\\d{2})?)",
            "inr\\s*(\\d+(?:\\.\\d{2})?)"
        )
        patterns.forEach { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).find(text)?.let { match ->
                return match.groupValues[1].toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractMerchantName(text: String): String? {
        val patterns = listOf(
            "to\\s+([^₹\\n]+?)(?:\\s+₹|\\n|\$)",
            "paid\\s+([^₹\\n]+?)(?:\\s+₹|\\n|\$)",
            "sent to\\s+([^₹\\n]+?)(?:\\s+₹|\\n|\$)"
        )
        patterns.forEach { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).find(text)?.let { match ->
                val name = match.groupValues[1].trim()
                if (name.isNotBlank() && name.length > 2) {
                    return name
                }
            }
        }
        return null
    }
}
