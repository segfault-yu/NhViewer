package com.example.nhviewer.data.remote.interceptor

import com.example.nhviewer.data.local.TokenManager
import com.example.nhviewer.data.remote.AuthApi
import com.example.nhviewer.data.remote.dto.TokenRefreshRequest
import dagger.Lazy
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
    private val authApiLazy: Lazy<AuthApi>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        val requestToken = response.request.header("Authorization")
        val currentToken = "Bearer ${tokenManager.getAccessToken()}"

        if (requestToken != currentToken) {
            return response.request.newBuilder()
                .header("Authorization", currentToken)
                .build()
        }

        return runBlocking {
            mutex.withLock {
                val newAccessToken = tokenManager.getAccessToken() ?: ""
                val currentTokenNow = "Bearer $newAccessToken"

                if (requestToken != currentTokenNow) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", currentTokenNow)
                        .build()
                }

                try {
                    val authApi = authApiLazy.get()
                    val refreshResponse = authApi.refresh(TokenRefreshRequest(refreshToken))
                    tokenManager.saveTokens(
                        accessToken = refreshResponse.accessToken,
                        refreshToken = refreshResponse.refreshToken
                    )
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                        .build()
                } catch (e: retrofit2.HttpException) {
                    if (e.code() in 400..403) {
                        tokenManager.clearTokens()
                    }
                    null
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
