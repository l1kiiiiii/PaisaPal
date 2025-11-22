package com.example.domain.strategy


import com.example.domain.model.*
import com.example.domain.repository.ContextSignatureRepository

class SoundBoxStrategy {

    suspend fun categorize(
        snapshot: ContextSnapshot,
        repository: ContextSignatureRepository
    ): CategoryResult {
        val bluetooth = snapshot.strongestBluetoothDevice ?: return CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.SOUNDBOX
        )

        // Query database for learned signature
        val signature = repository.getSignatureByTrigger(
            TriggerType.BLUETOOTH_MAC,
            bluetooth.macAddress
        )

        return if (signature != null) {
            // Known device - return learned category
            repository.incrementHitCount(signature.id)
            CategoryResult(
                category = signature.learnedCategory,
                confidence = calculateConfidence(signature.hitCount),
                strategy = CategorizationStrategy.SOUNDBOX,
                metadata = mapOf(
                    "mac" to bluetooth.macAddress,
                    "device" to bluetooth.deviceName
                )
            )
        } else {
            // Unknown device - flag for learning
            CategoryResult(
                category = "Uncategorized",
                confidence = 0.0f,
                strategy = CategorizationStrategy.SOUNDBOX,
                metadata = mapOf(
                    "mac" to bluetooth.macAddress,
                    "device" to bluetooth.deviceName,
                    "needsLearning" to "true"
                )
            )
        }
    }

    private fun calculateConfidence(hitCount: Int): Float {
        return when {
            hitCount >= 10 -> 0.95f
            hitCount >= 5 -> 0.85f
            hitCount >= 2 -> 0.75f
            else -> 0.65f
        }
    }
}
