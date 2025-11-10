package com.example.data.di

import com.example.domain.repository.SmsRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.ImportHistoricalSmsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Singleton
    @Provides
    fun provideImportHistoricalSmsUseCase(
        smsRepository: SmsRepository,
        transactionRepository: TransactionRepository
    ): ImportHistoricalSmsUseCase = ImportHistoricalSmsUseCase(smsRepository, transactionRepository)

}

