package com.tiarkaerell.ibstracker.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages authentication session persistence using EncryptedSharedPreferences
 *
 * Following Android best practices:
 * - Stores signed-in email locally for instant state restoration
 * - Uses EncryptedSharedPreferences for secure storage
 * - Eliminates "reconnection" flash on app restart
 */
class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If EncryptedSharedPreferences fails (e.g., corrupted), fall back to regular SharedPreferences
        Log.e(TAG, "Failed to create EncryptedSharedPreferences, using regular SharedPreferences", e)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save signed-in user email to local storage
     */
    fun saveSignedInEmail(email: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    /**
     * Get signed-in user email from local storage
     * Returns null if user is not signed in
     */
    fun getSignedInEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Clear all session data (sign out)
     */
    fun clearSession() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }

    companion object {
        private const val TAG = "SessionManager"
        private const val PREFS_NAME = "auth_session"
        private const val KEY_USER_EMAIL = "user_email"
        // v1.8.6 - Testing authentication persistence across app updates
    }
}
