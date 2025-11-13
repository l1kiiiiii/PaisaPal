package com.example.domain.repository

import com.example.domain.model.MerchantMapping
import kotlinx.coroutines.flow.Flow

interface MerchantMappingRepository {
    fun getAllMappings(): Flow<List<MerchantMapping>>
    suspend fun getMappingByKeyword(keyword: String): MerchantMapping?
    suspend fun insertMapping(mapping: MerchantMapping)
    suspend fun updateMapping(mapping: MerchantMapping)
    suspend fun deleteMapping(mapping: MerchantMapping)
    suspend fun incrementUsageCount(keyword: String)
}
