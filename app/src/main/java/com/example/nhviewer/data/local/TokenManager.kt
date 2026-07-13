package com.example.nhviewer.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences = try {
        createEncryptedPrefs(context)
    } catch (e: Throwable) {
        try {
            context.deleteSharedPreferences("secure_tokens_prefs")
            deleteMasterKeyEntry()
            createEncryptedPrefs(context)
        } catch (ex: Throwable) {
            context.getSharedPreferences("secure_tokens_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    private fun deleteMasterKeyEntry() {
        try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry("_androidx_security_master_key_")
        } catch (e: Throwable) {
            // 忽略 Keystore 清除失败的异常
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "secure_tokens_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun getExpiresAt(): Long {
        return sharedPreferences.getLong("expires_at", 0L)
    }

    fun isAccessTokenExpired(): Boolean {
        return System.currentTimeMillis() >= (getExpiresAt() - 60000L)
    }

    fun hasRefreshToken(): Boolean {
        return !getRefreshToken().isNullOrBlank()
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("expires_at")
            .apply()
    }
}
