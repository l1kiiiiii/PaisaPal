package com.example.data.di

import com.example.data.cache.NotificationCacheImpl
import com.example.data.local.LocationProviderImpl
import com.example.data.repository.UserCorrectionRepositoryImpl
import com.example.domain.data.NotificationCache
import com.example.domain.repository.LocationProvider
import com.example.domain.repository.UserCorrectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    @Singleton
    abstract fun bindNotificationCache(
        impl: NotificationCacheImpl
    ): NotificationCache

    @Binds
    @Singleton
    abstract fun bindLocationProvider(
        impl: LocationProviderImpl
    ): LocationProvider

    @Binds
    @Singleton
    abstract fun bindUserCorrectionRepository(
        impl: UserCorrectionRepositoryImpl
    ): UserCorrectionRepository
}
