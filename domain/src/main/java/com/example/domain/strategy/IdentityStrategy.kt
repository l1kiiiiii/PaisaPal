package com.example.domain.strategy


import com.example.domain.model.*
import com.example.domain.repository.ContextSignatureRepository

class IdentityStrategy {

    suspend fun categorize(
        snapshot: ContextSnapshot,
        repository: ContextSignatureRepository
    ): CategoryResult {
        val vpa = snapshot.upiVpa
        val merchantName = extractMerchantName(snapshot.rawSms)

        // Try VPA-based lookup
        if (vpa != null) {
            val signature = repository.getSignatureByTrigger(
                TriggerType.VPA_ID,
                vpa
            )
            if (signature != null) {
                repository.incrementHitCount(signature.id)
                return CategoryResult(
                    category = signature.learnedCategory,
                    confidence = 0.95f,
                    strategy = CategorizationStrategy.IDENTITY_VPA,
                    metadata = mapOf("vpa" to vpa)
                )
            }
        }

        // Try merchant name lookup
        if (merchantName != null) {
            val signature = repository.getSignatureByTrigger(
                TriggerType.VPA_ID,
                merchantName
            )
            if (signature != null) {
                repository.incrementHitCount(signature.id)
                return CategoryResult(
                    category = signature.learnedCategory,
                    confidence = 0.90f,
                    strategy = CategorizationStrategy.IDENTITY_VPA,
                    metadata = mapOf("merchant" to merchantName)
                )
            }
        }

        return CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.IDENTITY_VPA
        )
    }

    private fun extractMerchantName(sms: String): String? {
        // Extract merchant name from SMS patterns
        val patterns = listOf(
            Regex("to ([A-Z][A-Za-z\\s]+)", RegexOption.IGNORE_CASE),
            Regex("at ([A-Z][A-Za-z\\s]+)", RegexOption.IGNORE_CASE),
            Regex("from ([A-Z][A-Za-z\\s]+)", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(sms)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
}
