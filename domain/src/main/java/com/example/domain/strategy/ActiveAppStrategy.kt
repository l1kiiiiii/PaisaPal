package com.example.domain.strategy

import com.example.domain.model.CategoryResult
import com.example.domain.model.CategorizationStrategy
import com.example.domain.model.ContextSnapshot

class ActiveAppStrategy {

    private val appCategoryMap = mapOf(
        // Food Delivery
        "com.swiggy.android" to "Food",
        "com.application.zomato" to "Food",
        "com.ubereats" to "Food",

        // Transportation
        "com.ubercab" to "Transportation",
        "com.olacabs" to "Transportation",
        "in.rapido.passenger" to "Transportation",

        // Shopping
        "in.amazon.mShop.android.shopping" to "Shopping",
        "com.flipkart.android" to "Shopping",
        "com.myntra.android" to "Shopping",
        "com.meesho.supply" to "Shopping",

        // Payment
        "com.phonepe.app" to "Transfer",
        "net.one97.paytm" to "Transfer",
        "com.google.android.apps.nbu.paisa.user" to "Transfer",

        // Entertainment
        "com.netflix.mediaclient" to "Entertainment",
        "com.spotify.music" to "Entertainment",
        "in.startv.hotstar" to "Entertainment",

        // Utilities
        "com.google.android.apps.subscriptions.red" to "Utilities",
        "com.android.chrome" to "Shopping"
    )

    suspend fun categorize(snapshot: ContextSnapshot): CategoryResult {
        val foregroundApp = snapshot.foregroundAppPackage ?: return CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.ACTIVE_APP
        )

        val category = appCategoryMap[foregroundApp] ?: return CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.ACTIVE_APP
        )

        // High confidence if app matches and notification exists
        val confidence = if (snapshot.recentNotification != null) {
            0.9f
        } else {
            0.75f
        }

        return CategoryResult(
            category = category,
            confidence = confidence,
            strategy = CategorizationStrategy.ACTIVE_APP,
            metadata = mapOf("app" to foregroundApp)
        )
    }
}
