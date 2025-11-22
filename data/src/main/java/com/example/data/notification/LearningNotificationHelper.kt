package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class LearningNotificationHelper(private val context: Context) {

    private val channelId = "learning_channel"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun showLearningPrompt(transactionId: String, merchantInfo: String) {
        // Create intent to open your app (generic approach)
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("transaction_id", transactionId)
            putExtra("action", "categorize")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(
                context,
                transactionId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Transaction Detected")
            .setContentText("Help categorize transaction at $merchantInfo")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        notificationManager.notify(transactionId.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Transaction Learning",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for categorizing new transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
