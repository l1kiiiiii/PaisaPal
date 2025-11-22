package com.example.data.di


import com.example.data.repository.ContextSignatureRepositoryImpl
import com.example.domain.repository.ContextSignatureRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContextSignatureRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContextSignatureRepository(
        impl: ContextSignatureRepositoryImpl
    ): ContextSignatureRepository
}
