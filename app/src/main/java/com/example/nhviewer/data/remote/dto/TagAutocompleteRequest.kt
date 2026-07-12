package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagAutocompleteRequest(
    @SerialName("q") val q: String,
    @SerialName("type") val type: String? = null
)
