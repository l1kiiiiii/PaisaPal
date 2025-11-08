package com.example.domain.engine

import com.example.domain.data.MerchantRegistry
import com.example.domain.model.Transaction

class CategorizationEngine {

    fun categorize(transaction: Transaction): String? {
        // Step 1: Check merchant registry
        transaction.merchantDisplayName?.let { merchantName ->
            MerchantRegistry.getCategoryForMerchant(merchantName)?.let {
                return it
            }
        }

        // Step 2: Check UPI VPA
        transaction.upiVpa?.let { vpa ->
            val username = vpa.substringBefore("@").uppercase()
            MerchantRegistry.getCategoryForMerchant(username)?.let {
                return it
            }
        }

        // Step 3: Heuristic rules based on amount
        transaction.amount.let { amount ->
            when {
                amount in 1.0..100.0 && transaction.merchantDisplayName?.contains("CAF", ignoreCase = true) == true -> return "Food & Dining"
                amount in 100.0..500.0 && isLunchTime(transaction.timestamp) -> return "Food & Dining"
                amount > 10000.0 -> return "Transfer"
            }
        }

        // Step 4: Keywords in SMS body
        val smsLower = transaction.smsBody.lowercase()
        when {
            smsLower.contains("fuel") || smsLower.contains("petrol") || smsLower.contains("diesel") -> return "Fuel"
            smsLower.contains("electricity") || smsLower.contains("power") || smsLower.contains("water bill") -> return "Utilities"
            smsLower.contains("hospital") || smsLower.contains("clinic") || smsLower.contains("pharmacy") -> return "Health & Fitness"
            smsLower.contains("school") || smsLower.contains("college") || smsLower.contains("course") -> return "Education"
        }

        return null
    }

    private fun isLunchTime(timestamp: Long): Boolean {
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(java.util.Calendar.HOUR_OF_DAY)

        return hour in 12..14
    }

    fun autoCategorizeBatch(transactions: List<Transaction>): List<Transaction> {
        return transactions.map { transaction ->
            val category = categorize(transaction)
            transaction.copy(
                category = category,
                needsReview = category == null
            )
        }
    }
}
