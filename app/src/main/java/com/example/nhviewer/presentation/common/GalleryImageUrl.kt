package com.example.nhviewer.presentation.common

/** CDN 配置缺失时的兜底域名 */
const val FALLBACK_THUMB_HOST = "https://t.nhentai.net"
const val FALLBACK_IMAGE_HOST = "https://i.nhentai.net"

/** 宽高缺失时的封面比例 */
const val DEFAULT_COVER_RATIO = 0.7f

/** 规范化 CDN 域名：为空回退到 fallback，缺协议补 https */
fun normalizeCdnHost(cdnHost: String?, fallback: String): String = when {
    cdnHost.isNullOrBlank() -> fallback
    cdnHost.startsWith("http") -> cdnHost
    else -> "https://$cdnHost"
}

/**
 * 拼接画廊图片地址。
 * 所有列表与详情统一走这里，同一张图在各页面得到同一个 URL，Coil 才能跨页面命中同一份内存缓存。
 */
fun buildGalleryImageUrl(
    cdnHost: String?,
    path: String,
    fallbackHost: String = FALLBACK_THUMB_HOST
): String = "${normalizeCdnHost(cdnHost, fallbackHost)}/$path"

/** 宽高缺失时回退到常见封面比例，避免 aspectRatio 收到 0 或 NaN */
fun aspectRatioOf(width: Int, height: Int): Float =
    if (width > 0 && height > 0) width.toFloat() / height.toFloat() else DEFAULT_COVER_RATIO
