// domain/src/main/java/com/example/domain/engine/SenderAuthentication.kt
package com.example.domain.engine

import javax.inject.Inject

class SenderAuthentication @Inject constructor() {

    fun isAuthentic(sender: String): Boolean {
        return isBankSender(sender)
    }

    // Alias for backward compatibility
    fun isValidSender(sender: String): Boolean {
        return isAuthentic(sender)
    }

    //  Accept any bank/financial institution sender pattern
    private fun isBankSender(sender: String): Boolean {
        val normalized = sender.uppercase().trim()

        return when {
            // Pattern: XX-BANKNAME (e.g., VM-HDFCBK, AX-ICICIB, VM-YESBANK)
            normalized.matches(Regex("^[A-Z]{2}-[A-Z]+$")) -> true

            // Known wallets
            normalized in setOf(
                "PAYTM", "PHONEPE", "GPAY", "AMAZONP", "BHIM", "MOBIKWIK",
                "FREECHARGE", "PAYZAPP", "JIOMONEY"
            ) -> true

            // 5-7 letter bank codes
            normalized.matches(Regex("^[A-Z]{5,7}$")) -> true

            // Banks with dots/dashes (e.g., ICICI.B, HDFC-B)
            normalized.matches(Regex("^[A-Z]{3,6}[.-][A-Z]{1,2}$")) -> true

            else -> false
        }
    }

    fun isSpam(sender: String): Boolean {
        // Reject 10-digit numbers (personal numbers)
        if (sender.matches(Regex("^[0-9]{10}$"))) return true

        // Reject promotional senders
        val spamKeywords = listOf("OFFER", "WIN", "FREE", "GIFT", "PROMO", "DEAL", "SALE")
        return spamKeywords.any { sender.uppercase().contains(it) }
    }
}
