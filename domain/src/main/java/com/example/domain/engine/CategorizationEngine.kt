// domain/src/main/java/com/example/domain/engine/CategorizationEngine.kt
package com.example.domain.engine

import com.example.domain.data.MerchantRegistry
import com.example.domain.model.Transaction
import com.example.domain.repository.UserCorrectionRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CategorizationEngine @Inject constructor(
    private val userCorrectionRepository: UserCorrectionRepository
) {

    data class CategorizationResult(
        val category: String?,
        val confidence: Float,
        val method: CategorizationMethod
    )

    enum class CategorizationMethod {
        USER_LEARNED,      // From user corrections
        MERCHANT_REGISTRY, // Known merchants
        VPA_PATTERN,       // UPI VPA matching
        KEYWORD_BASED,     // Heuristic rules
        AMOUNT_BASED,      // Transaction amount patterns
        TIME_BASED,        // Time of day/week
        FALLBACK           // Default category
    }

    suspend fun categorizeWithConfidence(transaction: Transaction): CategorizationResult {
        // Strategy 1: User Learned (Highest Priority)
        userLearnedCategory(transaction)?.let {
            return CategorizationResult(it, 0.95f, CategorizationMethod.USER_LEARNED)
        }

        // Strategy 2: Merchant Registry
        merchantRegistryCategory(transaction)?.let {
            return CategorizationResult(it, 0.90f, CategorizationMethod.MERCHANT_REGISTRY)
        }

        // Strategy 3: VPA Pattern Matching
        vpaPatternCategory(transaction)?.let {
            return CategorizationResult(it, 0.80f, CategorizationMethod.VPA_PATTERN)
        }

        // Strategy 4: Advanced Keyword Matching
        keywordBasedCategory(transaction)?.let {
            return CategorizationResult(it, 0.65f, CategorizationMethod.KEYWORD_BASED)
        }

        // Strategy 5: Amount-Based Heuristics
        amountBasedCategory(transaction)?.let {
            return CategorizationResult(it, 0.50f, CategorizationMethod.AMOUNT_BASED)
        }

        // Strategy 6: Time-Based Patterns
        timeBasedCategory(transaction)?.let {
            return CategorizationResult(it, 0.45f, CategorizationMethod.TIME_BASED)
        }

        // Fallback: Unknown
        return CategorizationResult("Uncategorized", 0.30f, CategorizationMethod.FALLBACK)
    }

    // Legacy method for backward compatibility
    fun categorize(transaction: Transaction): String? {
        return runBlocking {
            categorizeWithConfidence(transaction).category
        }
    }

    // Strategy 1: User Learned Categories
    private suspend fun userLearnedCategory(transaction: Transaction): String? {
        // Check if user has previously corrected this merchant
        transaction.merchantDisplayName?.let { merchant ->
            return userCorrectionRepository.getCategoryForMerchant(merchant)
        }

        // Check UPI VPA
        transaction.upiVpa?.let { vpa ->
            val username = vpa.substringBefore("@")
            return userCorrectionRepository.getCategoryForMerchant(username)
        }

        return null
    }

    // Strategy 2: Merchant Registry (Keep existing logic)
    private fun merchantRegistryCategory(transaction: Transaction): String? {
        transaction.merchantDisplayName?.let { merchant ->
            MerchantRegistry.getCategoryForMerchant(merchant)?.let { return it }
        }

        transaction.upiVpa?.let { vpa ->
            val username = vpa.substringBefore("@").uppercase()
            MerchantRegistry.getCategoryForMerchant(username)?.let { return it }
        }

        return null
    }

    // Strategy 3: Enhanced VPA Pattern Matching
    private fun vpaPatternCategory(transaction: Transaction): String? {
        val vpa = transaction.upiVpa ?: return null

        return when {
            // Food delivery patterns
            vpa.contains("zomato", ignoreCase = true) ||
                    vpa.contains("swiggy", ignoreCase = true) ||
                    vpa.contains("ubereats", ignoreCase = true) -> "Food & Dining"

            // E-commerce patterns
            vpa.contains("amazon", ignoreCase = true) ||
                    vpa.contains("flipkart", ignoreCase = true) ||
                    vpa.contains("myntra", ignoreCase = true) -> "Shopping"

            // Utility patterns
            vpa.contains("electricity", ignoreCase = true) ||
                    vpa.contains("water", ignoreCase = true) ||
                    vpa.contains("gas", ignoreCase = true) -> "Utilities"

            // Entertainment patterns
            vpa.contains("netflix", ignoreCase = true) ||
                    vpa.contains("spotify", ignoreCase = true) ||
                    vpa.contains("prime", ignoreCase = true) -> "Entertainment"

            // Transport patterns
            vpa.contains("uber", ignoreCase = true) ||
                    vpa.contains("ola", ignoreCase = true) ||
                    vpa.contains("rapido", ignoreCase = true) -> "Transportation"

            else -> null
        }
    }

    // Strategy 4: Enhanced Keyword Matching
    private fun keywordBasedCategory(transaction: Transaction): String? {
        val text = buildString {
            append(transaction.smsBody.lowercase())
            append(" ")
            append(transaction.merchantRaw?.lowercase() ?: "")
            append(" ")
            append(transaction.merchantDisplayName?.lowercase() ?: "")
        }

        return when {
            // Food & Dining
            matchesAny(text, listOf(
                "restaurant", "cafe", "food", "zomato", "swiggy",
                "breakfast", "lunch", "dinner", "meal", "pizza",
                "burger", "coffee", "tea", "hotel", "canteen"
            )) -> "Food & Dining"

            // Shopping
            matchesAny(text, listOf(
                "amazon", "flipkart", "myntra", "shopping",
                "store", "retail", "mart", "mall", "clothes"
            )) -> "Shopping"

            // Groceries
            matchesAny(text, listOf(
                "grocery", "vegetables", "fruits", "milk",
                "bigbasket", "grofers", "blinkit", "zepto"
            )) -> "Groceries"

            // Transportation
            matchesAny(text, listOf(
                "uber", "ola", "rapido", "metro", "bus",
                "cab", "taxi", "parking", "toll", "petrol", "diesel"
            )) -> "Transportation"

            // Utilities
            matchesAny(text, listOf(
                "electricity", "water", "gas", "internet",
                "mobile", "recharge", "broadband", "wifi"
            )) -> "Utilities"

            // Healthcare
            matchesAny(text, listOf(
                "hospital", "doctor", "clinic", "pharmacy",
                "medicine", "medical", "lab", "test"
            )) -> "Healthcare"

            // Entertainment
            matchesAny(text, listOf(
                "movie", "cinema", "netflix", "spotify",
                "prime", "disney", "hotstar", "subscription"
            )) -> "Entertainment"

            // Transfer/P2P
            matchesAny(text, listOf(
                "transfer", "sent to", "paid to",
                "friend", "family", "personal"
            )) -> "Transfer"

            else -> null
        }
    }

    // Strategy 5: Amount-Based Heuristics
    private fun amountBasedCategory(transaction: Transaction): String? {
        return when (transaction.amount) {
            in 1.0..100.0 -> {
                // Small amounts + time heuristic
                if (isLunchTime(transaction.timestamp)) "Food & Dining"
                else if (isCommutingTime(transaction.timestamp)) "Transportation"
                else null
            }
            in 100.0..500.0 -> {
                if (isWeekday(transaction.timestamp)) "Food & Dining"
                else "Shopping"
            }
            in 500.0..2000.0 -> "Shopping"
            in 2000.0..10000.0 -> "Utilities"
            else -> null
        }
    }

    // Strategy 6: Time-Based Patterns
    private fun timeBasedCategory(transaction: Transaction): String? {
        return when {
            isLunchTime(transaction.timestamp) -> "Food & Dining"
            isCommutingTime(transaction.timestamp) -> "Transportation"
            isWeekendEvening(transaction.timestamp) -> "Entertainment"
            else -> null
        }
    }

    // Helper Functions
    private fun matchesAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }

    private fun isLunchTime(timestamp: Long): Boolean {
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 12..14
    }

    private fun isCommutingTime(timestamp: Long): Boolean {
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 7..10 || hour in 17..20
    }

    private fun isWeekday(timestamp: Long): Boolean {
        val dayOfWeek = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(java.util.Calendar.DAY_OF_WEEK)
        return dayOfWeek in 2..6 // Monday to Friday
    }

    private fun isWeekendEvening(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return (dayOfWeek == 1 || dayOfWeek == 7) && hour >= 18
    }
}
