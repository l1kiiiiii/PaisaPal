package com.example.data.local.dao


import androidx.room.*
import com.example.data.local.entity.UserCorrectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCorrectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(correction: UserCorrectionEntity)

    @Query("SELECT * FROM user_corrections WHERE merchantName = :merchantName")
    fun getCategoryForMerchant(merchantName: String): Flow<UserCorrectionEntity?>

    @Query("SELECT * FROM user_corrections ORDER BY timestamp DESC")
    fun getAllCorrections(): Flow<List<UserCorrectionEntity>>

    @Delete
    suspend fun delete(correction: UserCorrectionEntity)
}
