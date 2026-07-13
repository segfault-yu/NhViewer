package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostCommentRequest(
    @SerialName("body") val body: String,
    @SerialName("pow_solution") val powSolution: String,
    @SerialName("captcha_token") val captchaToken: String
)
