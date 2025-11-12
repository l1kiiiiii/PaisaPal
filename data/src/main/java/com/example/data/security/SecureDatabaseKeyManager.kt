package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages database encryption keys using Android Keystore
 * Keys are never stored in plain text
 */
class SecureDatabaseKeyManager(private val context: Context) {

    private val keyAlias = "paisapal_db_key"
    private val prefsFileName = "paisapal_secure_prefs"

    /**
     * Gets or generates the database encryption passphrase
     * Uses Android Keystore for secure key storage
     */
    fun getDatabasePassphrase(): ByteArray {
        return try {
            // Try to retrieve existing key
            getStoredKey() ?: generateAndStoreKey()
        } catch (e: Exception) {
            // Fallback: generate new key if retrieval fails
            generateAndStoreKey()
        }
    }

    private fun getStoredKey(): ByteArray? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encryptedPrefs = EncryptedSharedPreferences.create(
            prefsFileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val keyBase64 = encryptedPrefs.getString("db_key", null)
        return keyBase64?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
    }

    private fun generateAndStoreKey(): ByteArray {
        // Generate cryptographically secure random key
        val key = ByteArray(32) // 256 bits
        SecureRandom().nextBytes(key)

        // Store encrypted key
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encryptedPrefs = EncryptedSharedPreferences.create(
            prefsFileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val keyBase64 = android.util.Base64.encodeToString(key, android.util.Base64.DEFAULT)
        encryptedPrefs.edit().putString("db_key", keyBase64).apply()

        return key
    }

    /**
     * Clears the stored key (use when user logs out or resets app)
     */
    fun clearKey() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedPrefs = EncryptedSharedPreferences.create(
            prefsFileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        encryptedPrefs.edit().remove("db_key").apply()
    }
}
