package com.example.data.repository


import com.example.data.local.UserCorrectionDao
import com.example.data.local.entity.UserCorrectionEntity
import com.example.domain.repository.UserCorrectionRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UserCorrectionRepositoryImpl @Inject constructor(
    private val dao: UserCorrectionDao
) : UserCorrectionRepository {

    override suspend fun saveCorrection(merchantName: String, category: String) {
        dao.insert(UserCorrectionEntity(
            merchantName = merchantName.lowercase().trim(),
            category = category,
            timestamp = System.currentTimeMillis()
        ))
    }

    override suspend fun getCategoryForMerchant(merchantName: String): String? {
        return dao.getCategoryForMerchant(merchantName.lowercase().trim())
            .firstOrNull()
            ?.category
    }

    override suspend fun getAllCorrections(): Map<String, String> {
        return dao.getAllCorrections()
            .firstOrNull()
            ?.associate { it.merchantName to it.category }
            ?: emptyMap()
    }
}
