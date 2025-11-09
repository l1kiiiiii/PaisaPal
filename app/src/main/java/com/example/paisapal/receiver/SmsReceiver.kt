package com.example.paisapal.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.domain.engine.CategorizationEngine
import com.example.domain.engine.TransactionParser
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var parser: TransactionParser

    @Inject
    lateinit var categorizationEngine: CategorizationEngine

    @Inject
    lateinit var repository: TransactionRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            messages.forEach { sms ->
                val sender = sms.displayOriginatingAddress
                val body = sms.messageBody
                val timestamp = sms.timestampMillis

                Log.d(TAG, "SMS received from: $sender")

                // Process SMS in background
                CoroutineScope(Dispatchers.IO).launch {
                    processSms(sender, body, timestamp)
                }
            }
        }
    }

    private suspend fun processSms(sender: String, body: String, timestamp: Long) {
        // Check if it's a bank SMS
        if (!isBankSms(sender)) {
            Log.d(TAG, "Skipping non-bank SMS from: $sender")
            return
        }

        Log.d(TAG, "Processing bank SMS: $body")

        // Parse transaction
        val transaction = parser.parse(body, sender, timestamp)

        if (transaction != null) {
            // Auto-categorize
            val category = categorizationEngine.categorize(transaction)
            val categorizedTransaction = transaction.copy(
                category = category,
                needsReview = category == null
            )

            // Save to database
            repository.insert(categorizedTransaction)

            Log.d(TAG, "✅ Transaction saved: ${transaction.amount} - Category: ${category ?: "Uncategorized"}")
        } else {
            Log.d(TAG, "❌ Failed to parse SMS")
        }
    }

    private fun isBankSms(sender: String): Boolean {
        val bankKeywords = listOf(
            "bank", "hdfc", "icici", "sbi", "axis", "kotak", "indus",
            "paytm", "gpay", "phonepe", "amazon", "googlepay", "bhim"
        )
        return bankKeywords.any { sender.contains(it, ignoreCase = true) }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
