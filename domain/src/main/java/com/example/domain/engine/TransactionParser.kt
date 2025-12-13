package com.example.domain.engine

import com.example.domain.model.TransactionType

class TransactionParser {

    data class ParsedTransaction(
        val amount: Double,
        val type: TransactionType,
        val merchantRaw: String?,
        val upiVpa: String?,
        val referenceNumber: String?,
        val timestamp: Long,
        val availableBalance: Double? = null
    )

    fun parse(body: String, sender: String, timestamp: Long): ParsedTransaction? {
        val lowerBody = body.lowercase()

        // Detect transaction type
        val type = when {
            lowerBody.contains("credited") ||
                    lowerBody.contains("deposited") ||
                    lowerBody.contains("received") -> TransactionType.CREDIT

            lowerBody.contains("debited") ||
                    lowerBody.contains("withdrawn") ||
                    lowerBody.contains("paid") ||
                    lowerBody.contains("sent") -> TransactionType.DEBIT

            else -> return null
        }

        // Extract transaction amount (NOT balance)
        val amount = extractTransactionAmount(body) ?: return null

        return ParsedTransaction(
            amount = amount,
            type = type,
            merchantRaw = extractMerchant(body),
            upiVpa = extractUpiVpa(body),
            referenceNumber = extractReferenceNumber(body),
            timestamp = timestamp,
            availableBalance = extractAvailableBalance(body)
        )
    }

    /**
     * Extracts TRANSACTION amount, ignoring balance amounts.
     * Handles formats: Rs:25.00, Rs 25.00, Rs.25.00, ₹25.00
     */
    private fun extractTransactionAmount(body: String): Double? {
        //  Use negative lookbehind to avoid "Bal" amounts
        val patterns = listOf(
            // Avoid balance amounts with negative lookbehind
            "(?i)(?<!bal\\s|balance\\s|avl\\s|available\\s)(?:rs\\.?|inr|₹)\\s*[:=-]?\\s*([0-9,]+(?:\\.[0-9]{2})?)",

            // Debited/Credited amounts (high priority)
            "(?i)(?:debited|credited|paid|withdrawn|spent|charged)\\s+(?:rs\\.?|inr|₹)?\\s*([0-9,]+(?:\\.[0-9]{2})?)",

            // Transfer amounts
            "(?i)(?:transfer|sent|received)\\s+(?:of\\s+)?(?:rs\\.?|inr|₹)?\\s*([0-9,]+(?:\\.[0-9]{2})?)"
        )

        for (pattern in patterns) {
            val matches = Regex(pattern).findAll(body)
            for (match in matches) {
                val amountStr = match.groupValues[1].replace(",", "")
                val amount = amountStr.toDoubleOrNull()

                //  Validate it's a reasonable transaction amount
                if (amount != null && amount > 0 && amount < 10_000_000) {
                    return amount
                }
            }
        }

        return null
    }

    /**
     * Extracts available balance separately
     */
    private fun extractAvailableBalance(text: String): Double? {
        val pattern = "(?:avl\\s+bal|available\\s+balance|bal)\\s+(?:rs\\.?|inr|₹)?[:\\s]*([0-9,]+(?:\\.[0-9]{2})?)"
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        val match = regex.find(text)

        if (match != null) {
            val amountStr = match.groupValues[1].replace(",", "")
            return amountStr.toDoubleOrNull()
        }
        return null
    }

    /**
     * Extracts merchant name from SMS.
     * Supports: "to X", "at X", "from X", "via X", "by X"
     * Filters out banking terms like "Mob Bk", "Net Banking"
     */
    private fun extractMerchant(text: String): String? {
        val patterns = listOf(
            "(?:to|at|from|via|by)\\s+([A-Za-z0-9][A-Za-z0-9\\s&.'-]{1,40})(?:\\s+ref|\\s+utr|\\s+on|\\s+avl|\\s*$)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)

            if (match != null) {
                val merchant = match.groupValues[1].trim()

                // Filter out banking/system terms
                if (!isBankingTerm(merchant)) {
                    return cleanMerchantName(merchant)
                }
            }
        }
        return null
    }

    /**
     * Checks if extracted text is a banking term, not a merchant
     */
    private fun isBankingTerm(text: String): Boolean {
        val lower = text.lowercase().trim()
        val bankingTerms = setOf(
            "mob bk", "mobile banking", "net banking", "internet banking",
            "atm", "pos", "neft", "rtgs", "imps", "upi"
        )
        return bankingTerms.any { lower.contains(it) }
    }

    /**
     * Cleans merchant name: removes suffixes, title cases
     */
    private fun cleanMerchantName(merchantRaw: String): String {
        var cleaned = merchantRaw.trim()

        // Remove common suffixes
        val suffixesToRemove = listOf(
            " INDIA", " IND", " PVT LTD", " PVT. LTD.", " LTD", " LIMITED",
            " PRIVATE LIMITED", " PVT", " STORE", " STORES", " MART", " RETAIL"
        )

        for (suffix in suffixesToRemove) {
            if (cleaned.endsWith(suffix, ignoreCase = true)) {
                cleaned = cleaned.substring(0, cleaned.length - suffix.length).trim()
            }
        }

        // Title case
        cleaned = cleaned.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        return cleaned
    }

    /**
     * Extracts UPI VPA (Virtual Payment Address)
     * Format: username@bank
     */
    private fun extractUpiVpa(text: String): String? {
        val regex = Regex("([a-zA-Z0-9._-]+@[a-zA-Z]+)")
        return regex.find(text)?.groupValues?.get(1)
    }

    /**
     * Extracts reference/transaction number
     * Supports: 6-20 alphanumeric characters (flexible for different banks)
     */
    private fun extractReferenceNumber(text: String): String? {
        val patterns = listOf(
            // Standard: ref no 123456789
            "(?:ref|reference|utr|txn)\\s*(?:no\\.?|number)?\\s*:?\\s*([A-Z0-9]{6,20})",
            // UPI format: UPI Ref No 123456789012
            "UPI\\s+Ref\\s+No\\s+([0-9]{9,})"
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
