package com.example.domain.engine

import javax.inject.Inject

class SenderAuthentication @Inject constructor() {

    private val trustedSenders = setOf(
        // Banks
        "VM-HDFCBK", "VM-ICICIB", "VM-SBIIN", "VM-AXISBNK", "VM-KOTAKB",
        // Wallets
        "PAYTM", "PHONEPE", "GPAY", "AMAZONP", "BHIM",
        // Credit Cards
        "HDFCBK", "ICICIC", "SBCARD", "AXISCRD"
    )

    fun isAuthentic(sender: String): Boolean {
        // Check exact match
        if (trustedSenders.contains(sender.uppercase())) return true

        // Check prefix match (handles VM-HDFCBK, AX-HDFCBK variations)
        return trustedSenders.any { trusted ->
            sender.uppercase().contains(trusted) || trusted.contains(sender.uppercase())
        }
    }

    fun isSpam(sender: String): Boolean {
        // Reject 10-digit numbers (personal numbers)
        if (sender.matches(Regex("^[0-9]{10}$"))) return true

        // Reject promotional senders
        val spamKeywords = listOf("OFFER", "WIN", "FREE", "GIFT", "PROMO")
        return spamKeywords.any { sender.uppercase().contains(it) }
    }
}
