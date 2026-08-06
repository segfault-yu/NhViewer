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

        // 仅成功的 GET 响应值得客户端缓存；写操作与错误响应一律沿用服务端下发的头
        if (request.method != "GET" || !response.isSuccessful) {
            return response
        }

        val maxAge = resolveMaxAge(request.url.encodedPath) ?: return response

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            // private 而非 public：响应随 Authorization 变化，不允许共享缓存留存
            .header("Cache-Control", "private, max-age=$maxAge")
            .build()
    }

    /**
     * 白名单制，返回 null 表示该端点不做客户端缓存。
     * 随机画廊、收藏状态、评论、PoW 与验证码等必须实时获取，故不列入。
     */
    private fun resolveMaxAge(path: String): Int? = when {
        DETAIL_PATH.matches(path) || RELATED_PATH.matches(path) -> DETAIL_MAX_AGE
        path == GALLERIES_PATH || path == POPULAR_PATH || path == TAGGED_PATH -> LIST_MAX_AGE
        path == SEARCH_PATH -> LIST_MAX_AGE
        else -> null
    }

    private companion object {
        val DETAIL_PATH = Regex("""^/api/v2/galleries/\d+$""")
        val RELATED_PATH = Regex("""^/api/v2/galleries/\d+/related$""")
        const val GALLERIES_PATH = "/api/v2/galleries"
        const val POPULAR_PATH = "/api/v2/galleries/popular"
        const val TAGGED_PATH = "/api/v2/galleries/tagged"
        const val SEARCH_PATH = "/api/v2/search"

        const val DETAIL_MAX_AGE = 86400 // 详情 24 小时，配合详情页 5 分钟静默刷新兜住时效
        const val LIST_MAX_AGE = 600 // 列表与搜索 10 分钟
    }
}
