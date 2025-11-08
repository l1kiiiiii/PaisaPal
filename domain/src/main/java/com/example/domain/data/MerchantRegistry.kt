package com.example.domain.data


object MerchantRegistry {

    val MERCHANT_CATEGORIES = mapOf(
        // Food & Dining
        "ZOMATO" to "Food & Dining",
        "SWIGGY" to "Food & Dining",
        "DOMINOS" to "Food & Dining",
        "MCDONALD" to "Food & Dining",
        "KFC" to "Food & Dining",
        "STARBUCKS" to "Food & Dining",

        // Shopping
        "AMAZON" to "Shopping",
        "FLIPKART" to "Shopping",
        "MYNTRA" to "Shopping",

        // Grocery
        "BIGBASKET" to "Grocery",
        "BLINKIT" to "Grocery",

        // Travel
        "UBER" to "Transportation",
        "OLA" to "Transportation",
        "IRCTC" to "Travel",

        // Entertainment
        "NETFLIX" to "Entertainment",
        "AMAZON PRIME" to "Entertainment",

        // Utilities
        "JIO" to "Utilities",
        "AIRTEL" to "Utilities"
    )

    fun getCategoryForMerchant(merchantName: String): String? {
        val normalizedMerchant = merchantName.uppercase().trim()

        MERCHANT_CATEGORIES[normalizedMerchant]?.let { return it }

        for ((merchant, category) in MERCHANT_CATEGORIES) {
            if (normalizedMerchant.contains(merchant) || merchant.contains(normalizedMerchant)) {
                return category
            }
        }

        return null
    }
}