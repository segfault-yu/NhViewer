package com.example.nhviewer.data.remote.interceptor

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

// HTTP 缓存策略拦截器
class CacheStrategyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val forceRefresh = request.header("X-Force-Refresh") == "true"
        val builder = request.newBuilder().removeHeader("X-Force-Refresh")

        if (forceRefresh) {
            builder.cacheControl(CacheControl.FORCE_NETWORK)
            return chain.proceed(builder.build())
        }

        val response = chain.proceed(builder.build())
        val path = request.url.encodedPath

        val maxAge = when {
            path.contains("/api/gallery/") -> 86400 // 详情页缓存24小时
            path.contains("/api/galleries/") -> 600 // 列表页缓存10分钟
            else -> 300
        }

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", "public, max-age=$maxAge")
            .build()
    }
}
