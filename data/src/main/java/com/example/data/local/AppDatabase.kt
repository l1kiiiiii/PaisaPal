package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.ContextSignatureDao
import com.example.data.local.dao.MerchantMappingDao
import com.example.data.local.dao.NotificationCacheDao
import com.example.data.local.dao.SavedPlaceDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserCorrectionDao
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ContextSignatureEntity
import com.example.data.local.entity.MerchantMappingEntity
import com.example.data.local.entity.NotificationCacheEntity
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserCorrectionEntity


@Database(
    entities = [
        TransactionEntity::class,
        SavedPlaceEntity::class,
        MerchantMappingEntity::class,
        BudgetEntity::class,
        NotificationCacheEntity::class,
        UserCorrectionEntity::class,
        ContextSignatureEntity::class
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
    abstract fun userCorrectionDao(): UserCorrectionDao
    abstract fun contextSignatureDao(): ContextSignatureDao
}
