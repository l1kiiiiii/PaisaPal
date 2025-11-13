package com.example.data.local

import androidx.room.*
import com.example.data.local.entity.MerchantMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantMappingDao {

    @Query("SELECT * FROM merchant_mappings ORDER BY usageCount DESC, lastUsed DESC")
    fun getAllMappings(): Flow<List<MerchantMappingEntity>>

    @Query("SELECT * FROM merchant_mappings WHERE LOWER(merchantKeyword) = LOWER(:keyword) LIMIT 1")
    suspend fun getMappingByKeyword(keyword: String): MerchantMappingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: MerchantMappingEntity)

    @Update
    suspend fun updateMapping(mapping: MerchantMappingEntity)

    @Delete
    suspend fun deleteMapping(mapping: MerchantMappingEntity)

    @Query("UPDATE merchant_mappings SET usageCount = usageCount + 1, lastUsed = :timestamp WHERE LOWER(merchantKeyword) = LOWER(:keyword)")
    suspend fun incrementUsageCount(keyword: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM merchant_mappings")
    suspend fun deleteAll()
}
