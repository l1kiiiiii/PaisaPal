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

        // 1. Unified Keyword List (Both Debit & Credit)
        private val PAYMENT_KEYWORDS = listOf(
            "paid", "payment", "sent", "transferred", "debited", // Debit
            "received", "credited", "added",                     // Credit
            "₹", "rs", "rs.", "inr"
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName

            // Only listen to known payment apps (GPay, PhonePe, Paytm, etc.)
            if (!AppRegistry.isKnownPaymentApp(packageName)) return

            val notification = sbn.notification
            val extras = notification.extras
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

            // Combine all text for safer parsing
            val fullText = "$title $text $bigText".lowercase()

            if (!isPaymentNotification(fullText)) return

            // 2. Extract Raw Amount (Always Positive initially)
            val rawAmount = extractAmount(fullText) ?: return

            // 3. Determine Sign (Debit = Negative, Credit = Positive)
            val isCredit = isCreditTransaction(fullText)
            val signedAmount = if (isCredit) rawAmount else -rawAmount

            val counterparty = extractCounterparty(fullText, isCredit)
            val appName = AppRegistry.getAppInfo(packageName)?.displayName ?: "Unknown App"

            // 4. Save to Cache (No Interface Changes Needed!)
            // We pass the signed amount (-500.0 or +500.0) directly.
            if (rawAmount > 0) {
                notificationCache.addNotification(
                    amount = signedAmount,
                    merchantName = counterparty ?: "Unknown",
                    packageName = packageName,
                    appName = appName,
                    fullText = fullText,
                    timestamp = sbn.postTime
                )

                val typeStr = if (isCredit) "Credit (+)" else "Debit (-)"
                Log.d(TAG, "PaisaPal: ₹$signedAmount ($typeStr) detected from $appName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    private fun isPaymentNotification(text: String) =
        PAYMENT_KEYWORDS.any { text.contains(it) }

    /**
     * Returns TRUE if message contains Income keywords
     */
    private fun isCreditTransaction(text: String): Boolean {
        val creditKeywords = listOf("received from", "credited", "added to", "money received")
        return creditKeywords.any { text.contains(it) }
    }

    private fun extractAmount(text: String): Double? {
        val pattern = "(?:₹|rs\\.?|inr)\\s*([\\d,]+(?:\\.\\d{2})?)"
        Regex(pattern, RegexOption.IGNORE_CASE).find(text)?.let { match ->
            return match.groupValues[1].replace(",", "").toDoubleOrNull()
        }
        return null
    }

    /**
     * Smart Counterparty Extraction based on Transaction Type
     */
    private fun extractCounterparty(text: String, isCredit: Boolean): String? {
        val patterns = if (isCredit) {
            // INCOME: "Received from Rahul"
            listOf(
                "(?:received|credited)\\s+(?:₹|rs\\.?|inr)?\\s*[\\d,.]+\\s+from\\s+([^.\\n]+)",
                "(?:received|credited)\\s+from\\s+([^.\\n]+?)\\s+(?:₹|rs\\.?|inr)"
            )
        } else {
            // EXPENSE: "Paid to Zomato"
            listOf(
                "(?:paid|sent|transferred)\\s+(?:₹|rs\\.?|inr)?\\s*[\\d,.]+\\s+to\\s+([^.\\n]+)",
                "(?:paid|sent|transferred)\\s+(?:to\\s+)?([^.\\n]+?)\\s+(?:₹|rs\\.?|inr)"
            )
        }

        patterns.forEach { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).find(text)?.let { match ->
                val name = match.groupValues[1].trim()
                if (name.isNotBlank() && name.length in 2..40 && !name.contains("successful")) {
                    return name
                }
            }
        }
        return null
    }
}