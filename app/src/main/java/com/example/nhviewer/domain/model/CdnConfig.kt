package com.example.nhviewer.domain.model

data class CdnConfig(
    val primaryImageHost: String,
    val primaryThumbHost: String,
    // 全量镜像列表，供图片加载失败时按序切换重试；默认仅含主用主机自身
    val imageHosts: List<String> = listOf(primaryImageHost),
    val thumbHosts: List<String> = listOf(primaryThumbHost)
) {
    companion object {
        // 接口未加载成功前的兜底候选
        private val DEFAULT_IMAGE_HOSTS = listOf(
            "https://i1.nhentai.net",
            "https://i2.nhentai.net",
            "https://i3.nhentai.net",
            "https://i4.nhentai.net"
        )
        private val DEFAULT_THUMB_HOSTS = listOf(
            "https://t1.nhentai.net",
            "https://t2.nhentai.net",
            "https://t3.nhentai.net",
            "https://t4.nhentai.net"
        )
        val DEFAULT = CdnConfig(
            primaryImageHost = DEFAULT_IMAGE_HOSTS.first(),
            primaryThumbHost = DEFAULT_THUMB_HOSTS.first(),
            imageHosts = DEFAULT_IMAGE_HOSTS,
            thumbHosts = DEFAULT_THUMB_HOSTS
        )
    }
}
