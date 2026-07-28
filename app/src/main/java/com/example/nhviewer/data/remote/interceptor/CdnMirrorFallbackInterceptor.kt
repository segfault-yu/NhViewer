package com.example.nhviewer.data.remote.interceptor

import com.example.nhviewer.domain.repository.GalleryRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 图片镜像故障转移拦截器。
 * 接口返回的多台镜像服务器内容一致，单台不可用时按序切换重试，避免整页封面因此空白。
 */
class CdnMirrorFallbackInterceptor(
    private val galleryRepository: GalleryRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalHost = request.url.host
        val candidateHosts = resolveCandidateHosts(originalHost)

        candidateHosts.forEachIndexed { index, host ->
            val attemptRequest = if (host == originalHost) {
                request
            } else {
                request.newBuilder()
                    .url(request.url.newBuilder().host(host).build())
                    .build()
            }
            val isLastAttempt = index == candidateHosts.lastIndex
            try {
                val response = chain.proceed(attemptRequest)
                if (response.isSuccessful || isLastAttempt) {
                    return response
                }
                response.close()
            } catch (e: IOException) {
                if (isLastAttempt) throw e
            }
        }
        // 候选列表恒非空，末轮必定 return 或 throw，此处仅用于补全返回路径
        throw IOException("图片镜像服务器均不可用")
    }

    private fun resolveCandidateHosts(originalHost: String): List<String> {
        val config = galleryRepository.getCachedCdnConfig()
        val pool = when {
            config.thumbHosts.any { it.hostEquals(originalHost) } -> config.thumbHosts
            config.imageHosts.any { it.hostEquals(originalHost) } -> config.imageHosts
            else -> emptyList()
        }
        return (listOf(originalHost) + pool.mapNotNull { it.toHttpUrlOrNull()?.host }).distinct()
    }

    private fun String.hostEquals(host: String): Boolean = toHttpUrlOrNull()?.host == host
}
