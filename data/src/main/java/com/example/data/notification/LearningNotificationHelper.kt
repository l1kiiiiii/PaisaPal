package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.paisapal.R
import com.example.paisapal.MainActivity

class LearningNotificationHelper(private val context: Context) {

    private val channelId = "learning_channel"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun showLearningPrompt(transactionId: String, merchantInfo: String) {
        // For now, open MainActivity with transaction ID
        // You can create CategorySelectionActivity later
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("transaction_id", transactionId)
            putExtra("action", "categorize")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            transactionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Use default icon for now
            .setContentTitle("New Transaction Detected")
            .setContentText("Help categorize transaction at $merchantInfo")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
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
