package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.CdnConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CdnConfigDto(
    @SerialName("primary_image_host") val primaryImageHost: String,
    @SerialName("primary_thumb_host") val primaryThumbHost: String
)

fun CdnConfigDto.toDomain(): CdnConfig = CdnConfig(
    primaryImageHost = primaryImageHost,
    primaryThumbHost = primaryThumbHost
)
