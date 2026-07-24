package com.example.nhviewer.data.remote.interceptor

import com.example.nhviewer.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authHeader = tokenManager.getAuthHeaderValue()

        return if (authHeader != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
