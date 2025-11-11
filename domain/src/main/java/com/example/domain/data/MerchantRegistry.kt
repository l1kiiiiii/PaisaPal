package com.example.domain.data

object MerchantRegistry {

    private val merchantToCategory = mapOf(
        // Food & Dining
        "ZOMATO" to "Food & Dining",
        "SWIGGY" to "Food & Dining",
        "DOMINOS" to "Food & Dining",
        "MCDONALDS" to "Food & Dining",
        "KFC" to "Food & Dining",
        "SUBWAY" to "Food & Dining",
        "STARBUCKS" to "Food & Dining",
        "CAFE COFFEE DAY" to "Food & Dining",
        "CCD" to "Food & Dining",

        // Transportation
        "UBER" to "Transportation",
        "OLA" to "Transportation",
        "RAPIDO" to "Transportation",
        "METRO" to "Transportation",
        "IRCTC" to "Transportation",

        // Shopping
        "AMAZON" to "Shopping",
        "FLIPKART" to "Shopping",
        "MYNTRA" to "Shopping",
        "AJIO" to "Shopping",
        "MEESHO" to "Shopping",
        "NYKAA" to "Shopping",

        // Groceries
        "BLINKIT" to "Groceries",
        "GROFERS" to "Groceries",
        "BIGBASKET" to "Groceries",
        "DUNZO" to "Groceries",
        "ZEPTO" to "Groceries",
        "INSTAMART" to "Groceries",

        // Entertainment
        "NETFLIX" to "Entertainment",
        "PRIME VIDEO" to "Entertainment",
        "HOTSTAR" to "Entertainment",
        "DISNEY" to "Entertainment",
        "SPOTIFY" to "Entertainment",
        "YOUTUBE" to "Entertainment",
        "BOOKMYSHOW" to "Entertainment",

        // Utilities
        "ELECTRICITY" to "Utilities",
        "WATER" to "Utilities",
        "GAS" to "Utilities",
        "BROADBAND" to "Utilities",
        "AIRTEL" to "Utilities",
        "JIO" to "Utilities",
        "VI" to "Utilities",

        // Payment Apps
        "PAYTM" to "Payment",
        "PHONEPE" to "Payment",
        "GPAY" to "Payment",
        "GOOGLEPAY" to "Payment",
        "BHIM" to "Payment",
        "AMAZONPAY" to "Payment",

        // Health
        "APOLLO" to "Health & Fitness",
        "PRACTO" to "Health & Fitness",
        "1MG" to "Health & Fitness",
        "NETMEDS" to "Health & Fitness",
        "PHARMEASY" to "Health & Fitness",

        // Education
        "BYJU" to "Education",
        "UNACADEMY" to "Education",
        "COURSERA" to "Education",
        "UDEMY" to "Education"
    )

    fun getCategoryForMerchant(merchantName: String): String? {
        val upperMerchant = merchantName.uppercase().trim()

        // Exact match
        merchantToCategory[upperMerchant]?.let { return it }

        // Partial match (contains)
        merchantToCategory.entries.firstOrNull { (key, _) ->
            upperMerchant.contains(key) || key.contains(upperMerchant)
        }?.let { return it.value }

        return null
    }

    fun getAllMerchants(): Map<String, String> {
        return merchantToCategory
    }

    fun addMerchant(merchantName: String, category: String) {
        // This is an object, so we can't modify the map at runtime
        // In a real implementation, this would connect to a database
        // For now, this is just a placeholder
    }
}
