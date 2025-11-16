package com.example.domain.data

object AppRegistry {

    private val appToMerchantMap = mapOf(
        // Payment Apps
        "com.google.android.apps.nbu.paisa.user" to AppInfo("Google Pay", "Payment", "GPay"),
        "com.phonepe.app" to AppInfo("PhonePe", "Payment", "PhonePe"),
        "net.one97.paytm" to AppInfo("Paytm", "Payment", "Paytm"),
        "in.amazon.mShop.android.shopping" to AppInfo("Amazon Pay", "Payment", "Amazon Pay"),

        // Food Delivery
        "com.application.zomato" to AppInfo("Zomato", "Food & Dining", "Zomato"),
        "in.swiggy.android" to AppInfo("Swiggy", "Food & Dining", "Swiggy"),
        "com.ubercab.eats" to AppInfo("Uber Eats", "Food & Dining", "Uber Eats"),

        // Ride Sharing
        "com.ubercab" to AppInfo("Uber", "Transportation", "Uber"),
        "com.olacabs.customer" to AppInfo("Ola", "Transportation", "Ola"),
        "com.rapido.passenger" to AppInfo("Rapido", "Transportation", "Rapido"),

        // E-commerce
        "com.amazon.mShop.android.shopping" to AppInfo("Amazon", "Shopping", "Amazon"),
        "com.flipkart.android" to AppInfo("Flipkart", "Shopping", "Flipkart"),
        "com.myntra.android" to AppInfo("Myntra", "Shopping", "Myntra"),

        // Groceries
        "com.grofers.customerapp" to AppInfo("Blinkit", "Groceries", "Blinkit"),
        "com.bigbasket.mobileapp" to AppInfo("BigBasket", "Groceries", "BigBasket"),
        "com.dunzo.user" to AppInfo("Dunzo", "Groceries", "Dunzo"),

        // Entertainment
        "com.netflix.mediaclient" to AppInfo("Netflix", "Entertainment", "Netflix"),
        "com.spotify.music" to AppInfo("Spotify", "Entertainment", "Spotify"),
        "in.startv.hotstar" to AppInfo("Hotstar", "Entertainment", "Hotstar")
    )

    fun getAppInfo(packageName: String): AppInfo? {
        return appToMerchantMap[packageName]
    }

    fun isKnownPaymentApp(packageName: String): Boolean {
        return appToMerchantMap.containsKey(packageName)
    }

    data class AppInfo(
        val displayName: String,
        val category: String,
        val merchantName: String
    )
}