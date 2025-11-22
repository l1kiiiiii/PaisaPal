package com.example.domain.strategy

import com.example.domain.model.CategoryResult
import com.example.domain.model.CategorizationStrategy
import com.example.domain.model.ContextSnapshot

class TextStrategy {

    private val keywordMap = mapOf(
        "Food" to listOf("zomato", "swiggy", "restaurant", "food", "hotel", "cafe", "pizza", "burger"),
        "Transportation" to listOf("uber", "ola", "rapido", "cab", "taxi", "metro", "petrol", "fuel"),
        "Shopping" to listOf("amazon", "flipkart", "myntra", "shopping", "purchase", "order"),
        "Bills" to listOf("electricity", "bill", "recharge", "broadband", "gas", "water"),
        "Entertainment" to listOf("netflix", "movie", "ticket", "spotify", "game"),
        "Medical" to listOf("hospital", "doctor", "medical", "pharmacy", "medicine"),
        "Groceries" to listOf("grocery", "supermarket", "kirana", "vegetables")
    )

    suspend fun categorize(snapshot: ContextSnapshot): CategoryResult {
        val smsLower = snapshot.rawSms.lowercase()

        for ((category, keywords) in keywordMap) {
            for (keyword in keywords) {
                if (smsLower.contains(keyword)) {
                    return CategoryResult(
                        category = category,
                        confidence = 0.5f,
                        strategy = CategorizationStrategy.TEXT_FALLBACK,
                        metadata = mapOf("keyword" to keyword)
                    )
                }
            }
        }

        return CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.TEXT_FALLBACK
        )
    }
}
