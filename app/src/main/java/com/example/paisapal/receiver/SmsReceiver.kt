package com.example.paisapal.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
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
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        messages.forEach { message ->
            val sender = message.displayOriginatingAddress
            val body = message.messageBody
            val timestamp = message.timestampMillis

            Log.d(TAG, "SMS Received - Sender: $sender")

            scope.launch {
                try {
                    smsProcessingEngine.processIncomingSms(body, sender, timestamp)
                    Log.d(TAG, "SMS Processed Successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing SMS", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
