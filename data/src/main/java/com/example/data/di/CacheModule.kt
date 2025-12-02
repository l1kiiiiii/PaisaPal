package com.example.data.di


import com.example.data.cache.NotificationCacheImpl
import com.example.domain.data.NotificationCache
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    @Binds
    @Singleton
    abstract fun bindNotificationCache(
        impl: NotificationCacheImpl
    ): NotificationCache
}
