package com.example.paisapal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.paisapal.worker.TransactionMatchWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PaisaPalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleTransactionMatching()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_TRANSACTIONS,
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new transactions"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleTransactionMatching() {
        val matchingWork = PeriodicWorkRequestBuilder<TransactionMatchWorker>(
            15, // Run every 15 minutes
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "transaction_matching",
            ExistingPeriodicWorkPolicy.KEEP,
            matchingWork
        )
    }

    companion object {
        const val CHANNEL_ID_TRANSACTIONS = "transaction_channel"
    }
}
