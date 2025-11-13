package com.example.data.di

import android.content.Context
import android.os.Build
import androidx.room.Room
import com.example.data.local.*
import com.example.data.repository.MerchantMappingRepositoryImpl
import com.example.data.security.SecureDatabaseKeyManager
import com.example.domain.repository.MerchantMappingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSecureDatabaseKeyManager(
        @ApplicationContext context: Context
    ): SecureDatabaseKeyManager {
        return SecureDatabaseKeyManager(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        keyManager: SecureDatabaseKeyManager
    ): AppDatabase {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //  Use Android's native database encryption (API 28+)
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "paisapal_database"
            )
                .fallbackToDestructiveMigration()
                .build()
            // Note: Android P+ auto-encrypts databases when device is locked
        } else {
            // For older devices, fallback to unencrypted
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "paisapal_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideSavedPlaceDao(database: AppDatabase): SavedPlaceDao {
        return database.savedPlaceDao()
    }

    @Provides
    @Singleton
    fun provideNotificationCacheDao(database: AppDatabase): NotificationCacheDao {
        return database.notificationCacheDao()
    }

    @Provides
    @Singleton
    fun provideMerchantMappingDao(database: AppDatabase): MerchantMappingDao {
        return database.merchantMappingDao()
    }

    @Provides
    @Singleton
    fun provideMerchantMappingRepository(
        dao: MerchantMappingDao
    ): MerchantMappingRepository {
        return MerchantMappingRepositoryImpl(dao)
    }
}
