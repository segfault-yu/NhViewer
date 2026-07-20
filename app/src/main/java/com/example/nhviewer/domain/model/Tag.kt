package com.example.nhviewer.domain.model

data class Tag(
    val id: Int,
    val type: String,
    val name: String,
    val slug: String = "",
    val url: String = "",
    val count: Int = 0,
    val description: String? = null,
    val isCommunity: Boolean? = false
)
