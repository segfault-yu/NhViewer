package com.example.nhviewer.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class RateLimitInterceptor : Interceptor {
    private val limiters = ConcurrentHashMap<String, EndpointLimiter>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val normalizedKey = getNormalizedPath(path)
        
        val limiter = limiters.computeIfAbsent(normalizedKey) { EndpointLimiter() }
        limiter.acquire()
        
        return chain.proceed(request)
    }

    private fun getNormalizedPath(path: String): String {
        val segments = path.split("/").filter { it.isNotEmpty() }
        val normalizedSegments = segments.map { segment ->
            if (segment.all { it.isDigit() }) {
                "{id}"
            } else {
                segment
            }
        }
        return normalizedSegments.joinToString("/")
    }

    private class EndpointLimiter {
        private var lastRequestTime = 0L

        @Synchronized
        fun acquire() {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestTime
            val delay = 333L - elapsed
            if (delay > 0) {
                try {
                    Thread.sleep(delay)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            lastRequestTime = System.currentTimeMillis()
        }
    }
}
