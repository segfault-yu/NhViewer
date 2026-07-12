package com.example.nhviewer.domain.model

data class CdnConfig(
    val primaryImageHost: String,
    val primaryThumbHost: String
) {
    companion object {
        val DEFAULT = CdnConfig(
            primaryImageHost = "https://i.nhentai.net",
            primaryThumbHost = "https://t.nhentai.net"
        )
    }
}
