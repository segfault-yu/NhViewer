package com.example.nhviewer.util

import com.example.nhviewer.data.remote.interceptor.RateLimitException
import org.json.JSONObject
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
                val errorBody = try {
                    throwable.response()?.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }

                val serverErrorMessage = if (!errorBody.isNullOrBlank()) {
                    try {
                        val json = JSONObject(errorBody)
                        json.optString("error").takeIf { it.isNotBlank() }
                            ?: json.optString("detail").takeIf { it.isNotBlank() }
                            ?: json.optString("message").takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }

                if (serverErrorMessage != null) {
                    translateServerError(serverErrorMessage)
                } else {
                    when (throwable.code()) {
                        400 -> "客户端请求错误 (400)"
                        401 -> "身份验证失败，请重新登录 (401)"
                        403 -> "服务器拒绝访问 (403)"
                        404 -> "资源未找到 (404)"
                        429 -> "请求过于频繁，请稍后再试 (429)"
                        in 500..599 -> "服务器发生错误，请稍后再试 (${throwable.code()})"
                        else -> "网络请求失败，错误码：${throwable.code()}"
                    }
                }
            }
            is IOException -> "网络连接异常，请检查网络 (I/O 错误)"
            else -> throwable.localizedMessage ?: "发生未知错误"
        }
    }

    private fun translateServerError(msg: String): String {
        val lower = msg.lowercase()
        return when {
            lower.contains("username is already taken") -> "用户名已被占用"
            lower.contains("email is already taken") -> "邮箱已被占用"
            lower.contains("invalid username or password") -> "用户名或密码错误"
            lower.contains("invalid credentials") -> "用户名或密码错误"
            lower.contains("invalid captcha") -> "人机验证未通过"
            lower.contains("invalid proof of work") -> "验证计算失败 (PoW 验证错误)"
            lower.contains("password is too short") -> "密码长度不足"
            lower.contains("token expired") || lower.contains("expired token") -> "验证链接已过期"
            lower.contains("invalid token") -> "无效的验证链接或凭证"
            else -> msg
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
