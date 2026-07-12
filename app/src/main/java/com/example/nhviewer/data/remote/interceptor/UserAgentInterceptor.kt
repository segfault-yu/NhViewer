package com.example.nhviewer.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "NhentaiReader/1.0")
            .build()
        return chain.proceed(request)
    }
}
