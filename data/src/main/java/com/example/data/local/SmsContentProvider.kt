package com.example.data.local

import android.content.Context
import android.provider.Telephony
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class SmsMessage(
    val address: String,
    val body: String,
    val date: Long
)

class SmsContentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun readAllSms(): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

                Log.d(TAG, "Total SMS found: ${it.count}")

                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: continue
                    val date = it.getLong(dateIndex)

                    messages.add(SmsMessage(address, body, date))
                }
            }

            Log.d(TAG, "Processed ${messages.size} SMS messages")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS", e)
        }

        return messages
    }

    fun readSmsSince(timestamp: Long): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                "${Telephony.Sms.DATE} >= ?",  //  Filter by date
                arrayOf(timestamp.toString()),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

                Log.d(TAG, "SMS since $timestamp: ${it.count}")

                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: continue
                    val date = it.getLong(dateIndex)

                    messages.add(SmsMessage(address, body, date))
                }
            }

            Log.d(TAG, "Processed ${messages.size} recent SMS")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading recent SMS", e)
        }

        return messages
    }

    companion object {
        private const val TAG = "SmsContentProvider"
    }
}
