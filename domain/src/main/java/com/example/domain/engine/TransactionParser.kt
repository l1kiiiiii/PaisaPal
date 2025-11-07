package com.example.domain.engine

import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.util.UUID

class TransactionParser {

    fun parse(smsBody: String, sender: String, timestamp: Long): Transaction? {
        val type = detectType(smsBody) ?: return null
        val amount = extractAmount(smsBody) ?: return null
        val referenceNumber = extractReferenceNumber(smsBody)

        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            merchantRaw = null,
            merchantDisplayName = null,
            category = null,
            timestamp = timestamp,
            smsBody = smsBody,
            sender = sender,
            referenceNumber = referenceNumber,
            upiVpa = null,
            needsReview = true
        )
    }

    private fun detectType(smsBody: String): TransactionType? {
        val lower = smsBody.lowercase()
        return when {
            lower.contains("debited") || lower.contains("debit") ||
                    lower.contains("spent") || lower.contains("paid") -> TransactionType.DEBIT

            lower.contains("credited") || lower.contains("credit") ||
                    lower.contains("received") || lower.contains("deposited") -> TransactionType.CREDIT

            else -> null
        }
    }

    private fun extractAmount(smsBody: String): Double? {
        val patterns = listOf(
            """[Rr]s\.?\s*:?\s*(\d+(?:[,\.]\d{2})?)""".toRegex(),
            """₹\s*(\d+(?:[,\.]\d{2})?)""".toRegex(),
            """(?:amount|amt)\s*:?\s*[Rr]s\.?\s*(\d+(?:[,\.]\d{2})?)""".toRegex()
        )

        patterns.forEach { pattern ->
            pattern.find(smsBody)?.let { match ->
                return match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractReferenceNumber(smsBody: String): String? {
        val patterns = listOf(
            """ref\s*no\s*(\d+)""".toRegex(RegexOption.IGNORE_CASE),
            """ref\s*:\s*(\d+)""".toRegex(RegexOption.IGNORE_CASE),
            """utr\s*:?\s*(\d+)""".toRegex(RegexOption.IGNORE_CASE)
        )

        patterns.forEach { pattern ->
            pattern.find(smsBody)?.let { match ->
                return match.groupValues[1]
            }
        }
        return null
    }
}
