package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Manages database encryption keys using Android Keystore.
 * Keys are never stored in plain text.
 */
class SecureDatabaseKeyManager(private val context: Context) {

    private val prefsFileName = "paisapal_secure_prefs"
    private val keyEntryAlias = "db_key"

    // Lazy initialization for MasterKey to avoid recreating it unnecessarily
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    /**
     * Gets or generates the database encryption passphrase.
     * Uses Android Keystore for secure key storage.
     */
    fun getDatabasePassphrase(): ByteArray {
        return try {
            // Try to retrieve existing key, fallback to generating new one if fails
            getStoredKey() ?: generateAndStoreKey()
        } catch (_: Exception) {
            // If anything goes wrong (e.g., Keystore reset), generate a fresh key
            generateAndStoreKey()
        }
    }

    private fun getStoredKey(): ByteArray? {
        val encryptedPrefs = getEncryptedPrefs()
        val keyBase64 = encryptedPrefs.getString(keyEntryAlias, null)
        return keyBase64?.let { Base64.decode(it, Base64.DEFAULT) }
    }

    private fun generateAndStoreKey(): ByteArray {
        // 1. Generate cryptographically secure random key (256 bits)
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)

        // 2. Encode to Base64 for storage
        val keyBase64 = Base64.encodeToString(key, Base64.DEFAULT)

        // 3. Store in EncryptedSharedPreferences
        getEncryptedPrefs().edit().putString(keyEntryAlias, keyBase64).apply()

        return key
    }

    /**
     * Clears the stored key (use when user logs out or resets app).
     */
    fun clearKey() {
        getEncryptedPrefs().edit().remove(keyEntryAlias).apply()
    }

    /**
     * Helper function to get the EncryptedSharedPreferences instance.
     * Encapsulates the configuration logic (DRY).
     */
    private fun getEncryptedPrefs(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            prefsFileName,
            masterKey, // Use the new MasterKey object, not the deprecated string alias
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}