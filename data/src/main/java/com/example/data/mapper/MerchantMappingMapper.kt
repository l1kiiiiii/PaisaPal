package com.example.data.mapper

import com.example.data.local.entity.MerchantMappingEntity
import com.example.domain.model.MerchantMapping

fun MerchantMappingEntity.toDomain(): MerchantMapping {
    return MerchantMapping(
        id = id,
        merchantKeyword = merchantKeyword,
        category = category,
        userConfirmed = userConfirmed,
        usageCount = usageCount,
        lastUsed = lastUsed,
        createdAt = createdAt
    )
}

fun MerchantMapping.toEntity(): MerchantMappingEntity {
    return MerchantMappingEntity(
        id = id,
        merchantKeyword = merchantKeyword,
        category = category,
        userConfirmed = userConfirmed,
        usageCount = usageCount,
        lastUsed = lastUsed,
        createdAt = createdAt
    )
}
