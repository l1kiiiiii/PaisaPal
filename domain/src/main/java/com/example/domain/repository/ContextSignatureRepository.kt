package com.example.domain.repository


import com.example.domain.model.ContextSignature
import com.example.domain.model.TriggerType

interface ContextSignatureRepository {
    suspend fun getSignatureByTrigger(
        triggerType: TriggerType,
        triggerValue: String
    ): ContextSignature?

    suspend fun saveSignature(signature: ContextSignature)

    suspend fun updateSignature(signature: ContextSignature)

    suspend fun incrementHitCount(signatureId: String)
}
