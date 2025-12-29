package com.example.paisapal.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.domain.data.AppRegistry
import com.example.domain.engine.CategorizationEngine
import com.example.domain.engine.TransactionParser
import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import com.example.paisapal.worker.TransactionMatchWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class NotificationMonitorService : NotificationListenerService() {

    // 🧠 Inject the same engines used for SMS
    @Inject lateinit var transactionParser: TransactionParser
    @Inject lateinit var categorizationEngine: CategorizationEngine
    @Inject lateinit var transactionRepository: TransactionRepository
    @Inject lateinit var workManager: WorkManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "NotificationMonitor"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName

            // Only listen to known payment apps
            if (!AppRegistry.isKnownPaymentApp(packageName)) return

            val extras = sbn.notification.extras
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

            // Combine text for parser
            val fullContent = "$title . $text . $bigText"

            Log.d(TAG, "📱 Processing notification from $packageName")
            processNotification(fullContent, packageName, sbn.postTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    private fun processNotification(content: String, senderApp: String, timestamp: Long) {
        serviceScope.launch {
            try {
                //  REUSE: Use existing TransactionParser
                val parsed = transactionParser.parse(content, senderApp, timestamp) ?: run {
                    Log.d(TAG, " Parser rejected: Not a transaction")
                    return@launch
                }

                //  Build Transaction
                var transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = parsed.amount,
                    type = parsed.type,
                    merchantRaw = parsed.merchantRaw,
                    merchantDisplayName = null,
                    category = null,
                    timestamp = parsed.timestamp,
                    smsBody = content,
                    sender = senderApp,
                    referenceNumber = parsed.referenceNumber,
                    upiVpa = parsed.upiVpa,
                    needsReview = false,
                    accountLast4Digits = null,
                    accountName = "UPI App"
                )

                // 🧠 Categorize
                val categorizationResult = categorizationEngine.categorizeWithConfidence(transaction)
                transaction = transaction.copy(
                    category = categorizationResult.category,
                    needsReview = categorizationResult.confidence < 0.70f,
                    contextConfidence = categorizationResult.confidence
                )

                // 💾 Save to Database
                transactionRepository.insert(transaction)
                Log.i(TAG, " Notification Saved: ₹${transaction.amount} → ${transaction.category}")

                // 🧠 Trigger Matching
                triggerMatchingWorker()

            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    private fun triggerMatchingWorker() {
        Log.d(TAG, "📋 Scheduling Transaction Matching (5s delay)...")
        val matchRequest = OneTimeWorkRequestBuilder<TransactionMatchWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(matchRequest)
    }
}
