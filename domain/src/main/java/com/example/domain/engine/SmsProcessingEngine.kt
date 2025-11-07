package com.example.domain.engine

import com.example.domain.repository.TransactionRepository


class SmsProcessingEngine(
    private val parser: TransactionParser,
    private val repository: TransactionRepository
) {

    suspend fun processIncomingSms(smsBody: String, sender: String, timestamp: Long) {
        if (!isTrustedSender(sender)) return

        val transaction = parser.parse(smsBody, sender, timestamp) ?: return
        repository.insert(transaction)
    }

    private fun isTrustedSender(sender: String): Boolean {
        val keywords = listOf("bank", "hdfc", "icici", "sbi", "axis", "paytm", "gpay", "phonepe")
        return keywords.any { sender.contains(it, ignoreCase = true) }
    }
}
