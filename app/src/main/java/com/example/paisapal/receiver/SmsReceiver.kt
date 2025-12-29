package com.example.paisapal.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder  
import androidx.work.WorkManager                 
import com.example.data.service.TransactionProcessingService
import com.example.domain.engine.SmsProcessingEngine
import com.example.domain.model.SmsMessage
import com.example.domain.repository.TransactionRepository
import com.example.paisapal.worker.TransactionMatchWorker  
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit  
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsProcessingEngine: SmsProcessingEngine

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var transactionProcessingService: TransactionProcessingService

    @Inject
    lateinit var workManager: WorkManager  

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.w(TAG, "Context or Intent is null")
            return
        }

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.w(TAG, "Invalid action: ${intent.action}")
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "SMS permission not granted")
            return
        }

        val pendingResult: PendingResult = goAsync()

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting messages", e)
            pendingResult.finish()
            return
        }

        if (messages.isNullOrEmpty()) {
            Log.w(TAG, "No messages found")
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messagesBySender = messages.groupBy { it.displayOriginatingAddress }

                messagesBySender.forEach { (sender, fragments) ->
                    val fullBody = fragments.joinToString("") { it.messageBody }
                    val timestamp = fragments[0].timestampMillis

                    Log.d(TAG, "Processing SMS from: $sender")

                    try {
                        val uniqueId = "${sender}_${timestamp}_${fullBody.hashCode()}"

                        val smsMessage = SmsMessage(
                            id = uniqueId,
                            address = sender,
                            body = fullBody,
                            timestamp = timestamp,
                            type = 1
                        )

                        val transaction = smsProcessingEngine.processSms(smsMessage)

                        if (transaction != null) {
                            transactionRepository.insert(transaction)
                            Log.d(TAG, "✓ Transaction saved: ${transaction.amount}")

                            // Trigger context processing
                            launch {
                                try {
                                    transactionProcessingService.processTransaction(transaction)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Context processing failed", e)
                                }
                            }

                            // Trigger duplicate matching
                            triggerMatchingWorker()
                        } else {
                            Log.d(TAG, "SMS ignored (Not a transaction or filtered)")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing SMS from $sender", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error in SMS processing", e)
            } finally {
                pendingResult.finish()
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

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
