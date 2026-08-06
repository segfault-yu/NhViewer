package com.example.nhviewer.data.remote.interceptor

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

// HTTP 强制刷新拦截器
class ForceRefreshInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val forceRefresh = request.header("X-Force-Refresh") == "true"
        return if (forceRefresh) {
            val newRequest = request.newBuilder()
                .removeHeader("X-Force-Refresh")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}

// HTTP 缓存策略拦截器
class CacheStrategyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val path = request.url.encodedPath

        val maxAge = when {
            path.contains("/api/gallery/") -> 86400 // 详情页缓存24小时
            path.contains("/api/galleries/") -> 600 // 列表页缓存10分钟
            path.contains("/api/v2/search") || path.contains("/api/search") -> 600 // 搜索结果缓存10分钟
            else -> 300
        }

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", "public, max-age=$maxAge")
            .build()
    }
}
