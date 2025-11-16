// app/src/main/java/com/example/paisapal/util/SmsReader.kt
package com.example.paisapal.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.domain.engine.SmsProcessingEngine
import com.example.domain.model.SmsMessage
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsProcessingEngine: SmsProcessingEngine,
    private val transactionRepository: TransactionRepository
) {

    suspend fun readExistingSms(daysBack: Int = 30) = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)

            val cursor = context.contentResolver.query(
                Uri.parse("content://sms/inbox"),
                arrayOf("_id", "address", "body", "date"),
                "date >= ?",
                arrayOf(cutoffTime.toString()),
                "date DESC"
            )

            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")

                var processedCount = 0

                while (it.moveToNext()) {
                    val sender = it.getString(addressIndex)
                    val body = it.getString(bodyIndex)
                    val timestamp = it.getLong(dateIndex)

                    try {
                        // Create SmsMessage
                        val smsMessage = SmsMessage(
                            id = "${sender}_${timestamp}",
                            address = sender,
                            body = body,
                            timestamp = timestamp,
                            type = 1 // Inbox
                        )

                        // Process SMS
                        val transaction = smsProcessingEngine.processSms(smsMessage)

                        if (transaction != null) {
                            // Save to database
                            transactionRepository.insert(transaction)
                            processedCount++
                        }
                    } catch (e: Exception) {
                        Log.e("SmsReader", "Error processing SMS: ${e.message}")
                    }
                }

                Log.d("SmsReader", "Processed $processedCount existing SMS messages")
                processedCount
            } ?: 0
        } catch (e: Exception) {
            Log.e("SmsReader", "Error reading existing SMS", e)
            0
        }
    }
}
