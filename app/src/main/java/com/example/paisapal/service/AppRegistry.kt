package com.example.paisapal.service

import javax.inject.Inject
import javax.inject.Singleton

data class AppInfo(
    val packageName: String,
    val displayName: String,
    val category: String
)

@Singleton
class AppRegistry @Inject constructor() {

    companion object {
        private val knownApps = mapOf(
            // Food & Dining
            "com.application.zomato" to AppInfo(
                packageName = "com.application.zomato",
                displayName = "Zomato",
                category = "Food & Dining"
            ),
            "in.swiggy.android" to AppInfo(
                packageName = "in.swiggy.android",
                displayName = "Swiggy",
                category = "Food & Dining"
            ),
            "com.dominos" to AppInfo(
                packageName = "com.dominos",
                displayName = "Dominos",
                category = "Food & Dining"
            ),

            // Transportation
            "com.ubercab" to AppInfo(
                packageName = "com.ubercab",
                displayName = "Uber",
                category = "Transportation"
            ),
            "com.olacabs.customer" to AppInfo(
                packageName = "com.olacabs.customer",
                displayName = "Ola",
                category = "Transportation"
            ),
            "in.rapido.rider" to AppInfo(
                packageName = "in.rapido.rider",
                displayName = "Rapido",
                category = "Transportation"
            ),

            // Shopping
            "com.amazon.mShop.android.shopping" to AppInfo(
                packageName = "com.amazon.mShop.android.shopping",
                displayName = "Amazon",
                category = "Shopping"
            ),
            "com.flipkart.android" to AppInfo(
                packageName = "com.flipkart.android",
                displayName = "Flipkart",
                category = "Shopping"
            ),
            "com.myntra.android" to AppInfo(
                packageName = "com.myntra.android",
                displayName = "Myntra",
                category = "Shopping"
            ),
            "com.ajio.app" to AppInfo(
                packageName = "com.ajio.app",
                displayName = "AJIO",
                category = "Shopping"
            ),

            // Entertainment
            "com.netflix.mediaclient" to AppInfo(
                packageName = "com.netflix.mediaclient",
                displayName = "Netflix",
                category = "Entertainment"
            ),
            "com.spotify.music" to AppInfo(
                packageName = "com.spotify.music",
                displayName = "Spotify",
                category = "Entertainment"
            ),
            "com.amazon.avod.thirdpartyclient" to AppInfo(
                packageName = "com.amazon.avod.thirdpartyclient",
                displayName = "Prime Video",
                category = "Entertainment"
            ),
            "com.hotstar.android" to AppInfo(
                packageName = "com.hotstar.android",
                displayName = "Hotstar",
                category = "Entertainment"
            ),

            // Groceries
            "in.swiggy.instamart" to AppInfo(
                packageName = "in.swiggy.instamart",
                displayName = "Instamart",
                category = "Groceries"
            ),
            "com.blinkit" to AppInfo(
                packageName = "com.blinkit",
                displayName = "Blinkit",
                category = "Groceries"
            ),
            "in.grofers.customerapp" to AppInfo(
                packageName = "in.grofers.customerapp",
                displayName = "Blinkit",
                category = "Groceries"
            ),
            "com.bigbasket.mobileapp" to AppInfo(
                packageName = "com.bigbasket.mobileapp",
                displayName = "BigBasket",
                category = "Groceries"
            ),

            // Utilities
            "com.google.android.apps.nbu.paisa.user" to AppInfo(
                packageName = "com.google.android.apps.nbu.paisa.user",
                displayName = "Google Pay",
                category = "Payment"
            ),
            "net.one97.paytm" to AppInfo(
                packageName = "net.one97.paytm",
                displayName = "Paytm",
                category = "Payment"
            ),
            "com.phonepe.app" to AppInfo(
                packageName = "com.phonepe.app",
                displayName = "PhonePe",
                category = "Payment"
            ),

            // Health & Fitness
            "com.swiggy.instamart" to AppInfo(
                packageName = "com.swiggy.instamart",
                displayName = "Swiggy Instamart",
                category = "Groceries"
            ),
            "com.practo.fabric" to AppInfo(
                packageName = "com.practo.fabric",
                displayName = "Practo",
                category = "Healthcare"
            )
        )

        fun isKnownApp(packageName: String): Boolean {
            return knownApps.containsKey(packageName)
        }

        fun getAppInfo(packageName: String): AppInfo? {
            return knownApps[packageName]
        }

        fun getAllCategories(): Set<String> {
            return knownApps.values.map { it.category }.toSet()
        }
    }
}
