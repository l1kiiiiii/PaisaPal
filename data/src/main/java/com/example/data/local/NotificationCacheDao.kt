package com.example.data.local


import androidx.room.*
import com.example.data.local.entity.NotificationCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationCacheEntity)

    @Query("SELECT * FROM notification_cache WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getNotificationsInRange(startTime: Long, endTime: Long): List<NotificationCacheEntity>

    @Query("SELECT * FROM notification_cache WHERE amount = :amount AND timestamp BETWEEN :startTime AND :endTime LIMIT 1")
    suspend fun findByAmountAndTime(amount: Double, startTime: Long, endTime: Long): NotificationCacheEntity?

    @Query("DELETE FROM notification_cache WHERE timestamp < :cutoffTime")
    suspend fun deleteOldEntries(cutoffTime: Long)

    @Query("SELECT * FROM notification_cache ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationCacheEntity>>
}
