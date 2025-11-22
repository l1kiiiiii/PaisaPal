package com.example.data.repository

import com.example.data.local.dao.ContextSignatureDao
import com.example.data.local.entity.ContextSignatureEntity
import com.example.domain.model.ContextSignature
import com.example.domain.model.TriggerType
import com.example.domain.repository.ContextSignatureRepository
import javax.inject.Inject

class ContextSignatureRepositoryImpl @Inject constructor(
    private val dao: ContextSignatureDao
) : ContextSignatureRepository {

    override suspend fun getSignatureByTrigger(
        triggerType: TriggerType,
        triggerValue: String
    ): ContextSignature? {
        return dao.getByTrigger(triggerType.name, triggerValue)?.toDomain()
    }

    override suspend fun saveSignature(signature: ContextSignature) {
        dao.insert(signature.toEntity())
    }

    override suspend fun updateSignature(signature: ContextSignature) {
        dao.update(signature.toEntity())
    }

    override suspend fun incrementHitCount(signatureId: String) {
        val entity = dao.getById(signatureId)
        if (entity != null) {
            dao.update(
                entity.copy(
                    hitCount = entity.hitCount + 1,
                    lastUsedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun ContextSignatureEntity.toDomain() = ContextSignature(
        id = id,
        triggerType = TriggerType.valueOf(triggerType),
        triggerValue = triggerValue,
        learnedCategory = learnedCategory,
        confidenceScore = confidenceScore,
        hitCount = hitCount
    )

    private fun ContextSignature.toEntity() = ContextSignatureEntity(
        id = id,
        triggerType = triggerType.name,
        triggerValue = triggerValue,
        learnedCategory = learnedCategory,
        confidenceScore = confidenceScore,
        hitCount = hitCount,
        createdAt = System.currentTimeMillis(),
        lastUsedAt = System.currentTimeMillis()
    )
}
