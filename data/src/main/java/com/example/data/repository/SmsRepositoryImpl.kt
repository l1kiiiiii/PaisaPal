package com.example.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.domain.model.SmsMessage
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.SmsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsRepository {

    private val bankKeywords = listOf(
        "bank", "atm", "credited", "debited", "withdrawn", "deposited",
        "upi", "imps", "neft", "rtgs", "paytm", "phonepe", "googlepay",
        "gpay", "bhim", "amazonpay", "account", "card", "payment"
    )

    override suspend fun getAllSmsMessages(onlyBankSms: Boolean): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        // ✅ CRITICAL FIX: Check SMS permission before accessing
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("READ_SMS permission not granted")
        }

        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)

                while (cursor.moveToNext()) {
                    val body = cursor.getString(bodyIndex) ?: continue

                    // Filter for bank SMS if requested
                    if (onlyBankSms && !isBankSms(body)) {
                        continue
                    }

                    messages.add(
                        SmsMessage(
                            id = cursor.getString(idIndex),
                            address = cursor.getString(addressIndex) ?: "Unknown",
                            body = body,
                            timestamp = cursor.getLong(dateIndex),
                            type = cursor.getInt(typeIndex)
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read SMS messages", e)
        }

        return messages
    }

    // Keep all other existing methods unchanged
    override fun parseTransaction(smsMessage: SmsMessage): Transaction? {
        val body = smsMessage.body.lowercase()

        val type = when {
            body.contains("credited") || body.contains("deposited") || body.contains("received") -> TransactionType.CREDIT
            body.contains("debited") || body.contains("withdrawn") || body.contains("paid") || body.contains("sent") -> TransactionType.DEBIT
            else -> return null
        }

        val amount = extractAmount(smsMessage.body) ?: return null
        val merchant = extractMerchant(smsMessage.body)
        val upiVpa = extractUpiVpa(smsMessage.body)
        val refNumber = extractReferenceNumber(smsMessage.body)

        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            timestamp = smsMessage.timestamp,
            merchantRaw = merchant,
            merchantDisplayName = cleanMerchantName(merchant),
            upiVpa = upiVpa,
            referenceNumber = refNumber,
            category = null,
            sender = smsMessage.address,
            smsBody = smsMessage.body,
            needsReview = true
        )
    }

    private fun isBankSms(body: String): Boolean {
        val lowerBody = body.lowercase()
        return bankKeywords.any { lowerBody.contains(it) }
    }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            "(?:rs\\.?|inr|₹)\\s*([0-9,]+(?:\\.[0-9]{2})?)",
            "(?:amount|amt)\\s*(?:of)?\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,]+(?:\\.[0-9]{2})?)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return amountStr.toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractMerchant(text: String): String? {
        val patterns = listOf(
            "(?:to|at)\\s+([A-Z][A-Za-z0-9\\s&.-]{2,30})",
            "(?:from|via)\\s+([A-Z][A-Za-z0-9\\s&.-]{2,30})"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern)
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }

    private fun extractUpiVpa(text: String): String? {
        val regex = Regex("([a-zA-Z0-9._-]+@[a-zA-Z]+)")
        return regex.find(text)?.groupValues?.get(1)
    }

    private fun extractReferenceNumber(text: String): String? {
        val patterns = listOf(
            "(?:ref|reference|utr|txn)\\s*(?:no\\.?|number)?\\s*:?\\s*([A-Z0-9]{10,20})",
            "UPI Ref No\\s+([0-9]{12})"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private fun cleanMerchantName(merchant: String?): String? {
        return merchant?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.take(50)
    }
}
