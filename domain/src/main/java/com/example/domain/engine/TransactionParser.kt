package com.example.domain.engine

import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.util.UUID

class TransactionParser {

    fun parse(smsBody: String, sender: String, timestamp: Long): Transaction? {
        val type = detectType(smsBody) ?: return null
        val amount = extractAmount(smsBody) ?: return null
        val referenceNumber = extractReferenceNumber(smsBody)
        val upiVpa = extractUpiVpa(smsBody)
        val merchantRaw = extractMerchant(smsBody)
        val merchantDisplay = cleanMerchantName(merchantRaw)

        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            merchantRaw = merchantRaw,
            merchantDisplayName = merchantDisplay,
            upiVpa = upiVpa,
            category = null,
            timestamp = timestamp,
            smsBody = smsBody,
            sender = sender,
            referenceNumber = referenceNumber,
            needsReview = true
        )
    }

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

    private fun extractAmount(smsBody: String): Double? {
        val patterns = listOf(
            """(?:Rs\.?|INR)\s*[:=]?\s*([\d,]+\.?\d*)""".toRegex(RegexOption.IGNORE_CASE),
            """₹\s*([\d,]+\.?\d*)""".toRegex(),
            """(?:amount|amt|value)[:=]?\s*(?:Rs\.?|INR)?\s*([\d,]+\.?\d*)""".toRegex(RegexOption.IGNORE_CASE),
            """for\s+(?:Rs\.?|INR)\s*([\d,]+\.?\d*)""".toRegex(RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(smsBody)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return amountStr.toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractReferenceNumber(smsBody: String): String? {
        val patterns = listOf(
            """ref(?:erence)?\s*(?:no|number|#)?[:=]?\s*(\w+)""".toRegex(RegexOption.IGNORE_CASE),
            """utr\s*(?:no|number|#)?[:=]?\s*(\w+)""".toRegex(RegexOption.IGNORE_CASE),
            """transaction\s*(?:id|number|ref)?[:=]?\s*(\w+)""".toRegex(RegexOption.IGNORE_CASE),
            """txn(?:id)?[:=]?\s*(\w+)""".toRegex(RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(smsBody)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private fun extractMerchant(smsBody: String): String? {
        val patterns = listOf(
            """(?:at|from|to)\s+([A-Z][A-Z0-9\s&@.-]+?)(?:\s+on|\s+for|\s+via|$)""".toRegex(),
            """(?:paid|sent|transfer(?:red)?)\s+(?:to|at)\s+([A-Z][A-Z0-9\s&@.-]+?)(?:\s+on|\s+via|$)""".toRegex(),
            """merchant[:=]?\s*([A-Z][A-Za-z0-9\s&@.-]+?)(?:\s+on|$)""".toRegex(RegexOption.IGNORE_CASE),
            """([A-Z][A-Z0-9\s&@.-]+?)\s+via\s+(?:UPI|GPAY|PAYTM)""".toRegex(RegexOption.IGNORE_CASE),
            """(?:UPI-|VPA-)?([A-Z][A-Za-z0-9\s&@.-]+?)(?:@|$)""".toRegex(),
        )

        for (pattern in patterns) {
            val match = pattern.find(smsBody)
            if (match != null) {
                val merchant = match.groupValues[1].trim()
                if (merchant.length > 2 && !isCommonWord(merchant)) {
                    return merchant
                }
            }
        }

        extractUpiVpa(smsBody)?.let { vpa ->
            val username = vpa.substringBefore("@")
            if (username.length > 2) {
                return username.uppercase()
            }
        }

        return null
    }

    private fun extractUpiVpa(smsBody: String): String? {
        val pattern = """([\w.-]+@[\w.-]+)""".toRegex()
        val match = pattern.find(smsBody)
        return match?.value
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
}
