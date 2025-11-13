package com.example.data.repository

import com.example.data.local.MerchantMappingDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.MerchantMapping
import com.example.domain.repository.MerchantMappingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MerchantMappingRepositoryImpl @Inject constructor(
    private val dao: MerchantMappingDao
) : MerchantMappingRepository {

    override fun getAllMappings(): Flow<List<MerchantMapping>> {
        return dao.getAllMappings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMappingByKeyword(keyword: String): MerchantMapping? {
        return dao.getMappingByKeyword(keyword)?.toDomain()
    }

    override suspend fun insertMapping(mapping: MerchantMapping) {
        dao.insertMapping(mapping.toEntity())
    }

    override suspend fun updateMapping(mapping: MerchantMapping) {
        dao.updateMapping(mapping.toEntity())
    }

    override suspend fun deleteMapping(mapping: MerchantMapping) {
        dao.deleteMapping(mapping.toEntity())
    }

    override suspend fun incrementUsageCount(keyword: String) {
        dao.incrementUsageCount(keyword)
    }
}
