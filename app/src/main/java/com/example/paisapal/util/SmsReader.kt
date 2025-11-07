package com.example.paisapal.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.domain.engine.SmsProcessingEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsProcessingEngine: SmsProcessingEngine
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

                    // Process through the same engine
                    smsProcessingEngine.processIncomingSms(body, sender, timestamp)
                    processedCount++
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
