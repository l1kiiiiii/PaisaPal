package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.SavedPlaceDao
import com.example.data.local.AppDatabase
import com.example.data.local.SmsContentProvider
import com.example.data.local.TransactionDao
import com.example.data.repository.SavedPlaceRepositoryImpl
import com.example.data.repository.TransactionRepositoryImpl
import com.example.domain.repository.SavedPlaceRepository
import com.example.domain.repository.TransactionRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "paisapal_database"
        ).build()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideSavedPlaceDao(database: AppDatabase): SavedPlaceDao {
        return database.savedPlaceDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSavedPlaceRepository(dao: SavedPlaceDao): SavedPlaceRepository {
        return SavedPlaceRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSmsContentProvider(@ApplicationContext context: Context): SmsContentProvider {
        return SmsContentProvider(context)
    }
}
