package com.example.data.local

import android.content.Context
import android.provider.Telephony


class SmsContentProvider(private val context: Context) {

    /**
     * Reads all SMS from device storage
     * Returns list of SMS with sender, body, and timestamp
     */
    fun readAllSms(): List<SmsData> {
        val smsList = mutableListOf<SmsData>()

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,        // Sender
                    Telephony.Sms.BODY,           // Message content
                    Telephony.Sms.DATE,           // Timestamp
                    Telephony.Sms.TYPE            // Type (1=received, 2=sent)
                ),
                "${Telephony.Sms.TYPE} = ?",
                arrayOf("1"),                      // Only received SMS
                "${Telephony.Sms.DATE} DESC"     // Newest first
            )

            cursor?.use { c ->
                while (c.moveToNext()) {
                    val sender = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val date = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))

                    smsList.add(SmsData(sender, body, date))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return smsList
    }

    /**
     * Read SMS from specific sender (e.g., only bank SMS)
     */
    fun readSmsByAddress(sender: String): List<SmsData> {
        val smsList = mutableListOf<SmsData>()

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                "${Telephony.Sms.ADDRESS} LIKE ? AND ${Telephony.Sms.TYPE} = ?",
                arrayOf("%$sender%", "1"),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use { c ->
                while (c.moveToNext()) {
                    val smsAddress = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val date = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))

                    smsList.add(SmsData(smsAddress, body, date))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return smsList
    }

    /**
     * Read SMS from last N days
     */
    fun readSmsFromLastDays(days: Int): List<SmsData> {
        val smsList = mutableListOf<SmsData>()
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                "${Telephony.Sms.DATE} > ? AND ${Telephony.Sms.TYPE} = ?",
                arrayOf(cutoffTime.toString(), "1"),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use { c ->
                while (c.moveToNext()) {
                    val sender = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val date = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))

                    smsList.add(SmsData(sender, body, date))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return smsList
    }
}

data class SmsData(
    val sender: String,
    val body: String,
    val timestamp: Long
)
