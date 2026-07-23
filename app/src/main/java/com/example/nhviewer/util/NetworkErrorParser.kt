package com.example.nhviewer.util

import com.example.nhviewer.data.remote.interceptor.RateLimitException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorParser {
    fun parse(throwable: Throwable): String {
        return when (throwable) {
            is RateLimitException -> "请求过于频繁，请在 ${throwable.retryAfterSeconds} 秒后重试"
            is SocketTimeoutException -> "网络连接超时，请稍后重试"
            is UnknownHostException -> "无法解析服务器地址，请检查网络连接"
            is ConnectException -> "无法连接到服务器，请检查网络连接"
            is HttpException -> {
                when (throwable.code()) {
                    429 -> "请求过于频繁，请稍后再试 (429)"
                    in 500..599 -> "服务器发生错误，请稍后再试 (${throwable.code()})"
                    else -> "网络请求失败，错误码：${throwable.code()}"
                }
            }
            is IOException -> "网络连接异常，请检查网络 (I/O 错误)"
            else -> throwable.localizedMessage ?: "发生未知错误"
        }
    }
}

inline fun <T> runCatchingCancelable(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
