package com.example.domain.engine

import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.util.UUID

class TransactionParser {

    private fun detectType(smsBody: String): TransactionType? {
        val lower = smsBody.lowercase()
        return when {
            lower.contains("debited") || lower.contains("debit") ||
                    lower.contains("spent") || lower.contains("paid") ||
                    lower.contains("withdrawn") || lower.contains("purchase") -> TransactionType.DEBIT

            lower.contains("credited") || lower.contains("credit") ||
                    lower.contains("received") || lower.contains("deposited") ||
                    lower.contains("refund") || lower.contains("cashback") -> TransactionType.CREDIT

            else -> null
        }
    }
    private fun cleanMerchantName(merchantRaw: String?): String? {
        if (merchantRaw == null) return null

        var cleaned = merchantRaw.trim()

        val suffixesToRemove = listOf(
            " INDIA", " IND", " PVT LTD", " PVT. LTD.", " LTD", " LIMITED",
            " PRIVATE LIMITED", " PVT", " STORE", " STORES", " MART"
        )

        for (suffix in suffixesToRemove) {
            if (cleaned.endsWith(suffix, ignoreCase = true)) {
                cleaned = cleaned.substring(0, cleaned.length - suffix.length).trim()
            }
        }

        cleaned = cleaned.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        return cleaned
    }

    private fun isCommonWord(word: String): Boolean {
        val commonWords = setOf(
            "TRANSACTION", "PAYMENT", "TRANSFER", "AMOUNT", "ACCOUNT", "BANK",
            "CARD", "DEBIT", "CREDIT", "BALANCE", "AVAILABLE", "INFO", "ALERT"
        )
        return commonWords.contains(word.uppercase())
    }
    data class ParsedTransaction(
        val amount: Double,
        val type: TransactionType,
        val merchantRaw: String?,
        val upiVpa: String?,
        val referenceNumber: String?,
        val timestamp: Long
    )

    fun parse(body: String, sender: String, timestamp: Long): ParsedTransaction? {
        val lowerBody = body.lowercase()

        val type = when {
            lowerBody.contains("credited") || lowerBody.contains("deposited") || lowerBody.contains("received") -> TransactionType.CREDIT
            lowerBody.contains("debited") || lowerBody.contains("withdrawn") || lowerBody.contains("paid") || lowerBody.contains("sent") -> TransactionType.DEBIT
            else -> return null
        }

        val amount = extractAmount(body) ?: return null

        return ParsedTransaction(
            amount = amount,
            type = type,
            merchantRaw = extractMerchant(body),
            upiVpa = extractUpiVpa(body),
            referenceNumber = extractReferenceNumber(body),
            timestamp = timestamp
        )
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
}
