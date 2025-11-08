package com.example.data.local

import androidx.room.*
import com.example.data.local.entity.MerchantMappingEntity

@Dao
interface MerchantMappingDao {

    @Query("SELECT * FROM merchant_mappings WHERE upiVpa = :upiVpa LIMIT 1")
    suspend fun getByUpiVpa(upiVpa: String): MerchantMappingEntity?

    @Query("SELECT * FROM merchant_mappings")
    suspend fun getAllMappings(): List<MerchantMappingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: MerchantMappingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mappings: List<MerchantMappingEntity>)

    @Delete
    suspend fun delete(mapping: MerchantMappingEntity)
}
