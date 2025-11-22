package com.example.domain.learning

import com.example.domain.model.ContextSignature
import com.example.domain.model.ContextSnapshot
import com.example.domain.model.TriggerType
import com.example.domain.repository.ContextSignatureRepository
import java.util.*
import javax.inject.Inject

class ContextLearningEngine @Inject constructor(
    private val signatureRepository: ContextSignatureRepository
) {

    suspend fun createSignatureFromUserFeedback(
        contextSnapshot: ContextSnapshot,
        userSelectedCategory: String
    ) {
        // Determine which trigger to use based on available context
        val triggerInfo = determineBestTrigger(contextSnapshot) ?: return

        val signature = ContextSignature(
            id = UUID.randomUUID().toString(),
            triggerType = triggerInfo.first,
            triggerValue = triggerInfo.second,
            learnedCategory = userSelectedCategory,
            confidenceScore = 0.7f, // Initial confidence
            hitCount = 1
        )

        signatureRepository.saveSignature(signature)
    }

    suspend fun updateSignatureFromCorrection(
        transactionId: String,
        oldCategory: String,
        newCategory: String,
        contextSnapshot: ContextSnapshot
    ) {
        val triggerInfo = determineBestTrigger(contextSnapshot) ?: return

        // Try to find existing signature
        val existingSignature = signatureRepository.getSignatureByTrigger(
            triggerInfo.first,
            triggerInfo.second
        )

        if (existingSignature != null && existingSignature.learnedCategory == oldCategory) {
            // Update to new category
            val updated = existingSignature.copy(
                learnedCategory = newCategory,
                hitCount = 1 // Reset hit count for re-learning
            )
            signatureRepository.updateSignature(updated)
        } else {
            // Create new signature
            createSignatureFromUserFeedback(contextSnapshot, newCategory)
        }
    }

    suspend fun pruneOldSignatures() {
        // TODO: Implement pruning logic
        // Delete signatures with hitCount < 3 and older than 30 days
    }

    private fun determineBestTrigger(
        contextSnapshot: ContextSnapshot
    ): Pair<TriggerType, String>? {
        // Priority: Bluetooth > VPA > App Package
        contextSnapshot.strongestBluetoothDevice?.let {
            return TriggerType.BLUETOOTH_MAC to it.macAddress
        }

        contextSnapshot.upiVpa?.let {
            return TriggerType.VPA_ID to it
        }

        contextSnapshot.foregroundAppPackage?.let {
            return TriggerType.APP_PACKAGE to it
        }

        return null
    }
}
