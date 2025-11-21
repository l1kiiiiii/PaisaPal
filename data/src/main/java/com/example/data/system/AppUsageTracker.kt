package com.example.data.system


import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager

class AppUsageTracker(private val context: Context) {

    private val usageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    private val knownAppCategories = mapOf(
        "com.swiggy.android" to AppCategory.FOOD,
        "com.application.zomato" to AppCategory.FOOD,
        "com.ubercab" to AppCategory.TRANSPORTATION,
        "com.olacabs" to AppCategory.TRANSPORTATION,
        "in.amazon.mShop.android.shopping" to AppCategory.SHOPPING,
        "com.flipkart.android" to AppCategory.SHOPPING,
        "com.phonepe.app" to AppCategory.PAYMENT,
        "net.one97.paytm" to AppCategory.PAYMENT,
        "com.google.android.apps.nbu.paisa.user" to AppCategory.PAYMENT
    )

    fun getForegroundAppAtTime(timestamp: Long): String? {
        if (!hasUsageStatsPermission()) return null

        val startTime = timestamp - 1000L // 1 second before
        val endTime = timestamp + 500L // 0.5 second after

        val events = usageStatsManager?.queryEvents(startTime, endTime) ?: return null

        var lastForegroundApp: String? = null

        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundApp = event.packageName
            }
        }

        return lastForegroundApp
    }

    fun getCategoryForPackage(packageName: String): AppCategory? {
        return knownAppCategories[packageName]
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps?.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps?.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}

// Add to domain/model/AppCategory.kt
enum class AppCategory {
    FOOD,
    TRANSPORTATION,
    SHOPPING,
    PAYMENT,
    UNKNOWN
}
