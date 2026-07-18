package com.example.nhviewer.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.example.nhviewer.domain.model.AuthEvent
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

    private val _accessTokenFlow = MutableStateFlow<String?>(getAccessToken())
    val accessTokenFlow: StateFlow<String?> = _accessTokenFlow.asStateFlow()

    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 64)
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

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
        _accessTokenFlow.value = accessToken
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
        _accessTokenFlow.value = null
    }

    fun emitSessionExpired() {
        _authEvents.tryEmit(AuthEvent.SessionExpired)
    }
}
