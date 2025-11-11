package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.MerchantMappingEntity
import com.example.data.local.entity.NotificationCacheEntity
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        SavedPlaceEntity::class,
        MerchantMappingEntity::class,
        BudgetEntity::class,
        NotificationCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationCacheDao(): NotificationCacheDao
}
