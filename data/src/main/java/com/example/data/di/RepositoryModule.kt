package com.example.data.di


import com.example.data.repository.TransactionRepositoryImpl
import com.example.data.repository.SmsRepositoryImpl
import com.example.domain.repository.TransactionRepository
import com.example.domain.repository.SmsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository
}

