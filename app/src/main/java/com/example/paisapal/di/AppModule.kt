package com.example.paisapal.di

import com.example.domain.engine.SmsProcessingEngine
import com.example.domain.engine.TransactionParser
import com.example.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTransactionParser(): TransactionParser {
        return TransactionParser()
    }

    @Provides
    @Singleton
    fun provideSmsProcessingEngine(
        parser: TransactionParser,
        repository: TransactionRepository
    ): SmsProcessingEngine {
        return SmsProcessingEngine(parser, repository)
    }
}
