package com.example.domain.engine

import com.example.domain.model.CategoryResult
import com.example.domain.model.CategorizationStrategy
import com.example.domain.model.ContextSnapshot
import com.example.domain.repository.ContextSignatureRepository
import com.example.domain.strategy.*

class ContextAwareCategorizer(
    private val signatureRepository: ContextSignatureRepository,
    private val activeAppStrategy: ActiveAppStrategy,
    private val soundBoxStrategy: SoundBoxStrategy,
    private val identityStrategy: IdentityStrategy,
    private val patternStrategy: PatternStrategy,
    private val textStrategy: TextStrategy
) {
    private val confidenceThreshold = 0.8f

    suspend fun categorize(snapshot: ContextSnapshot): CategoryResult {
        // Strategy A: Active App
        val appResult = activeAppStrategy.categorize(snapshot)
        if (appResult.confidence >= confidenceThreshold) {
            return appResult
        }

        // Strategy B: SoundBox
        val soundBoxResult = soundBoxStrategy.categorize(snapshot, signatureRepository)
        if (soundBoxResult.confidence >= confidenceThreshold) {
            return soundBoxResult
        }

        // Strategy C: Identity/VPA
        val identityResult = identityStrategy.categorize(snapshot, signatureRepository)
        if (identityResult.confidence >= confidenceThreshold) {
            return identityResult
        }

        // Strategy D: Pattern (Heuristic)
        val patternResult = patternStrategy.categorize(snapshot)
        if (patternResult.confidence >= confidenceThreshold) {
            return patternResult
        }

        // Strategy E: Text Fallback
        val textResult = textStrategy.categorize(snapshot)
        if (textResult.confidence >= confidenceThreshold) {
            return textResult
        }

        // Return best effort result (highest confidence)
        return listOf(appResult, soundBoxResult, identityResult, patternResult, textResult)
            .maxByOrNull { it.confidence } ?: CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.UNCATEGORIZED
        )
    }
}
