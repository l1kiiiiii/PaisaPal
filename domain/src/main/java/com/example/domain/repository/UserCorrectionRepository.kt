package com.example.domain.repository


interface UserCorrectionRepository {
    suspend fun saveCorrection(merchantName: String, category: String)
    suspend fun getCategoryForMerchant(merchantName: String): String?
    suspend fun getAllCorrections(): Map<String, String>
}
