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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsProcessingEngine: SmsProcessingEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        // Null safety check
        if (context == null || intent == null) {
            Log.w(TAG, "Context or Intent is null")
            return
        }

        // Verify action
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.w(TAG, "Invalid action: ${intent.action}")
            return
        }

        // ✅ CRITICAL FIX: Check SMS permission at runtime
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "SMS permission not granted. Cannot process SMS.")
            return
        }

        // ✅ CRITICAL FIX: Use goAsync() to prevent ANR
        val pendingResult = goAsync()

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            if (messages == null || messages.isEmpty()) {
                Log.w(TAG, "No messages found in intent")
                pendingResult.finish()
                return
            }

            messages.forEach { message ->
                val sender = message.displayOriginatingAddress
                val body = message.messageBody
                val timestamp = message.timestampMillis

                Log.d(TAG, "SMS Received - Sender: $sender")

                scope.launch {
                    try {
                        smsProcessingEngine.processIncomingSms(sender, body, timestamp)
                        Log.d(TAG, "SMS Processed Successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing SMS", e)
                    } finally {
                        // ✅ Always release the wakelock
                        pendingResult.finish()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive", e)
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
