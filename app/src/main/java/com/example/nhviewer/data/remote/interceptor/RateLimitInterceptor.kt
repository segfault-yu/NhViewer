package com.example.nhviewer.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class RateLimitException(val retryAfterSeconds: Long) : IOException("Rate limit exceeded. Retry after $retryAfterSeconds seconds.")

class RateLimitInterceptor : Interceptor {
    private val limiters = ConcurrentHashMap<String, EndpointLimiter>()
    private val endpointLockTimes = ConcurrentHashMap<String, AtomicLong>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val normalizedKey = getNormalizedPath(path)
        val lockAtomic = endpointLockTimes.computeIfAbsent(normalizedKey) { AtomicLong(0L) }

        val unlockTime = lockAtomic.get()
        val now = System.currentTimeMillis()
        if (now < unlockTime) {
            val remainingSeconds = (unlockTime - now + 999) / 1000
            throw RateLimitException(remainingSeconds)
        }

        val limiter = limiters.computeIfAbsent(normalizedKey) { EndpointLimiter() }
        limiter.acquire()

        val response = chain.proceed(request)

        if (response.code == 429) {
            val retryAfterHeader = response.header("Retry-After")
            val retryAfterSeconds = retryAfterHeader?.toLongOrNull() ?: 60L
            lockAtomic.set(System.currentTimeMillis() + retryAfterSeconds * 1000)
        }

        return response
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
