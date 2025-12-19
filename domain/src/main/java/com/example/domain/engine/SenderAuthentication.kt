// domain/src/main/java/com/example/domain/engine/SenderAuthentication.kt
package com.example.domain.engine

import javax.inject.Inject

class SenderAuthentication @Inject constructor() {

    fun isAuthentic(sender: String): Boolean {
        return isAuthenticSender(sender)
    }

    fun isValidSender(sender: String): Boolean {
        return isAuthentic(sender)
    }

    //  Stricter sender validation
    private fun isAuthenticSender(sender: String): Boolean {
        val normalized = sender.replace("+91", "").trim()

        //  10-digit mobile numbers (personal SMS)
       // if (normalized.matches(Regex("^\\d{10}$"))) return false

        //  Short codes (1-4 digits) - usually spam/VAS
        if (normalized.matches(Regex("^\\d{1,4}$"))) return false

        //  Bank sender patterns
        return when {
            // Pattern: XX-BANKNAME (e.g., VM-HDFCBK, AX-ICICIB)
            normalized.matches(Regex("^[A-Z]{2}-[A-Z0-9]+$")) -> true

            // 6-digit alphanumeric (e.g., HDFCBK, 123456)
            normalized.matches(Regex("^[A-Z0-9]{6}$")) -> true

            // Known wallets (exact match)
            normalized.uppercase() in setOf(
                "PAYTM", "PHONEPE", "GPAY", "AMAZONP", "BHIM", "MOBIKWIK",
                "FREECHARGE", "PAYZAPP", "JIOMONEY"
            ) -> true

            else -> false
        }
    }

    //  Comprehensive spam detection
    fun isSpam(sender: String): Boolean {
        val upperSender = sender.uppercase()
        val bodySpamKeywords = listOf(
            // Authentication/Security (NOT transactions)
            "OTP", "LOGIN", "VERIFY", "VERIFICATION", "PASSWORD", "PIN",
            "AUTHENTICATE", "CODE", "PASSCODE", "UNLOCK",

            // Marketing/Promotional
            "OFFER", "WIN", "FREE", "GIFT", "PROMO", "DEAL", "SALE",
            "DISCOUNT", "CASHBACK", "REWARD", "BONUS", "LUCKY",

            // Gaming/Betting
            "RUMMY", "POKER", "DREAM11", "TEEN PATTI", "FANTASY",
            "BET", "BETTING", "CASINO", "GAME",

            // KYC/Document (informational, not transactional)
            "KYC", "PAN", "AADHAAR", "CKYC", "E-KYC", "DOCUMENT",

            // Alerts/Notifications (not money movement)
            "ALERT", "REMINDER", "DUE", "EXPIRY", "RENEW",

            // Statement/Info requests
            "STATEMENT", "MINI STATEMENT", "LAST 5", "BALANCE ENQUIRY"
        )

        return bodySpamKeywords.any { upperSender.contains(it) }
    }

    //  Check if message body is spam (call this in SmsProcessingEngine)
    fun isBodySpam(messageBody: String): Boolean {
        val upperBody = messageBody.uppercase()
        val bodySpamPhrases = listOf(
            "OTP IS", "YOUR OTP", "ONE TIME PASSWORD", "VERIFICATION CODE",
            "LOGIN OTP", "DO NOT SHARE", "EXPIRES IN",
            "CLICK HERE", "DOWNLOAD APP", "INSTALL NOW",
            "CONGRATULATIONS", "YOU WON", "CLAIM NOW"
        )

        return bodySpamPhrases.any { upperBody.contains(it) }
    }
}
