package com.example.data.di

import com.example.data.local.dao.TransactionDao
import android.content.Context
import com.example.data.context.ContextGatherer
import com.example.data.permission.PermissionManager
import com.example.data.remote.BluetoothManager
import com.example.data.service.TransactionProcessingService
import com.example.data.settings.SensorSettings
import com.example.data.system.AppUsageTracker
import com.example.data.system.LocationProvider
import com.example.domain.engine.ContextAwareCategorizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContextProcessingModule {

    @Provides
    @Singleton
    fun provideBluetoothManager(
        @ApplicationContext context: Context
    ): BluetoothManager {
        return BluetoothManager(context)
    }

    @Provides
    @Singleton
    fun provideAppUsageTracker(
        @ApplicationContext context: Context
    ): AppUsageTracker {
        return AppUsageTracker(context)
    }

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context
    ): LocationProvider {
        return LocationProvider(context)
    }

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideSensorSettings(
        @ApplicationContext context: Context
    ): SensorSettings {
        return SensorSettings(context)
    }

    @Provides
    @Singleton
    fun provideContextGatherer(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager,
        appUsageTracker: AppUsageTracker,
        locationProvider: LocationProvider,
        permissionManager: PermissionManager,
        sensorSettings: SensorSettings
    ): ContextGatherer {
        return ContextGatherer(
            context = context,
            bluetoothManager = bluetoothManager,
            appUsageTracker = appUsageTracker,
            locationProvider = locationProvider,
            permissionManager = permissionManager,
            sensorSettings = sensorSettings
        )
    }

    @Provides
    @Singleton
    fun provideTransactionProcessingService(
        contextGatherer: ContextGatherer,
        categorizer: ContextAwareCategorizer,
        transactionDao: TransactionDao,
        @ApplicationContext context: Context
    ): TransactionProcessingService {
        return TransactionProcessingService(
            contextGatherer = contextGatherer,
            categorizer = categorizer,
            transactionDao = transactionDao,
            context = context
        )
    }
}
