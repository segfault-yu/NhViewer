package com.example.nhviewer.data.remote.interceptor

import com.example.nhviewer.data.local.TokenManager
import com.example.nhviewer.data.remote.AuthApi
import com.example.nhviewer.data.remote.dto.TokenRefreshRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        val requestToken = response.request.header("Authorization")
        val currentToken = tokenManager.getAuthHeaderValue() ?: ""

        if (requestToken != currentToken) {
            return response.request.newBuilder()
                .header("Authorization", currentToken)
                .build()
        }

        return runBlocking {
            mutex.withLock {
                val newAccessToken = tokenManager.getAccessToken()
                if (newAccessToken.isNullOrBlank()) {
                    return@runBlocking null
                }
                val currentTokenNow = tokenManager.formatAuthHeader(newAccessToken)

                if (requestToken != currentTokenNow) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", currentTokenNow)
                        .build()
                }

                try {
                    val refreshResponse = authApi.refresh(TokenRefreshRequest(refreshToken))
                    tokenManager.saveTokens(
                        accessToken = refreshResponse.accessToken,
                        refreshToken = refreshResponse.refreshToken
                    )
                    com.example.nhviewer.util.log.AppLogger.i("AUTH", "Token refreshed successfully")
                    response.request.newBuilder()
                        .header("Authorization", tokenManager.formatAuthHeader(refreshResponse.accessToken))
                        .build()
                } catch (e: retrofit2.HttpException) {
                    com.example.nhviewer.util.log.AppLogger.w("AUTH", "Token refresh failed with code ${e.code()}", e)
                    if (e.code() in 400..403) {
                        tokenManager.clearTokens()
                        tokenManager.emitSessionExpired()
                    }
                    null
                } catch (e: Exception) {
                    com.example.nhviewer.util.log.AppLogger.e("AUTH", "Unexpected error refreshing token", e)
                    null
                }
            }
        }
    }
}
