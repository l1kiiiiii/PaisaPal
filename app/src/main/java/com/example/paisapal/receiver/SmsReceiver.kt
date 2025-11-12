package com.example.paisapal.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.domain.engine.SmsProcessingEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsProcessingEngine: SmsProcessingEngine

    override fun onReceive(context: Context?, intent: Intent?) {
        // Null safety checks
        if (context == null || intent == null) {
            Log.w(TAG, "Context or Intent is null")
            return
        }

        // Verify action
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.w(TAG, "Invalid action: ${intent.action}")
            return
        }

        // Check SMS permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "SMS permission not granted")
            return
        }

        //  Use goAsync() to extend receiver lifetime
        val pendingResult: PendingResult = goAsync()

        // Extract messages
        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting messages", e)
            pendingResult.finish() // Always cleanup
            return
        }

        if (messages == null || messages.isEmpty()) {
            Log.w(TAG, "No messages found")
            pendingResult.finish()
            return
        }

        //  Process SMS in background with proper cleanup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                messages.forEach { message ->
                    val sender = message.displayOriginatingAddress
                    val body = message.messageBody
                    val timestamp = message.timestampMillis

                    Log.d(TAG, "Processing SMS from: $sender")

                    try {
                        // Process the SMS
                        smsProcessingEngine.processIncomingSms(sender, body, timestamp)
                        Log.d(TAG, " SMS processed successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error processing SMS from $sender", e)
                        // Continue processing other messages
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error in SMS processing", e)
            } finally {
                //  ALWAYS release the wakelock
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
