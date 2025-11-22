package com.example.paisapal.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.service.TransactionProcessingService
import com.example.domain.engine.SmsProcessingEngine
import com.example.domain.model.SmsMessage
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsProcessingEngine: SmsProcessingEngine

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var transactionProcessingService: TransactionProcessingService

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

        if (messages == null || messages.isEmpty()) {
            Log.w(TAG, "No messages found")
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                messages.forEach { message ->
                    val sender = message.displayOriginatingAddress
                    val body = message.messageBody
                    val timestamp = message.timestampMillis

                    Log.d(TAG, "Processing SMS from: $sender")

                    try {
                        // STEP 1: Create SmsMessage (EXISTING LOGIC)
                        val smsMessage = SmsMessage(
                            id = "${sender}_${timestamp}",
                            address = sender,
                            body = body,
                            timestamp = timestamp,
                            type = 1 // Inbox
                        )

                        // STEP 2: Process SMS with existing engine (EXISTING LOGIC)
                        val transaction = smsProcessingEngine.processSms(smsMessage)

                        if (transaction != null) {
                            // STEP 3: Save to database (EXISTING LOGIC)
                            transactionRepository.insert(transaction)
                            Log.d(TAG, "✓ Transaction saved: ${transaction.amount}")

                            // STEP 4: NEW - Trigger context-aware processing
                            // This runs asynchronously and won't block SMS processing
                            launch {
                                try {
                                    transactionProcessingService.processTransaction(transaction)
                                    Log.d(TAG, "✓ Context-aware processing initiated for: ${transaction.id}")
                                } catch (e: Exception) {
                                    Log.e(TAG, "❌ Context processing failed for ${transaction.id}", e)
                                    // Don't fail the whole SMS processing if context fails
                                }
                            }
                        } else {
                            Log.d(TAG, "SMS not a transaction")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error processing SMS from $sender", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error in SMS processing", e)
            } finally {
                try {
                    pendingResult.finish()
                    Log.d(TAG, "PendingResult finished")
                } catch (e: Exception) {
                    Log.e(TAG, "Error finishing pendingResult", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
