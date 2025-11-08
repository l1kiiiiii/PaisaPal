package com.example.data.local

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsContentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class SmsMessage(
        val address: String,
        val body: String,
        val date: Long
    )

    fun readAllSms(): List<SmsMessage> {
        val smsList = mutableListOf<SmsMessage>()
        val uri = Uri.parse("content://sms/inbox")

        val cursor = context.contentResolver.query(
            uri,
            arrayOf("address", "body", "date"),
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex("address")
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")

            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)

                smsList.add(SmsMessage(address, body, date))
            }
        }

        return smsList
    }

    fun readSmsSince(timestamp: Long): List<SmsMessage> {
        val smsList = mutableListOf<SmsMessage>()
        val uri = Uri.parse("content://sms/inbox")

        val cursor = context.contentResolver.query(
            uri,
            arrayOf("address", "body", "date"),
            "date > ?",
            arrayOf(timestamp.toString()),
            "date DESC"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex("address")
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")

            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)

                smsList.add(SmsMessage(address, body, date))
            }
        }

        return smsList
    }
}
