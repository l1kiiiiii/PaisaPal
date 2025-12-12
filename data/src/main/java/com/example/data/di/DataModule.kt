package com.example.data.di

import android.content.Context
import android.os.Build
import androidx.room.Room
import com.example.data.local.*
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.ContextSignatureDao
import com.example.data.local.dao.MerchantMappingDao
import com.example.data.local.dao.NotificationCacheDao
import com.example.data.local.dao.SavedPlaceDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserAccountDao
import com.example.data.local.dao.UserCorrectionDao
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
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "paisapal_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        } else {
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

    @Provides
    @Singleton
    fun provideUserCorrectionDao(database: AppDatabase): UserCorrectionDao {
        return database.userCorrectionDao()
    }


    @Provides
    @Singleton
    fun provideContextSignatureDao(database: AppDatabase): ContextSignatureDao {
        return database.contextSignatureDao()
    }

    @Provides
    @Singleton
    fun provideUserAccountDao(database: AppDatabase): UserAccountDao {
        return database.userAccountDao()
    }
}
