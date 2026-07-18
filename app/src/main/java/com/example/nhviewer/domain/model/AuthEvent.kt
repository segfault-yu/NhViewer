package com.example.nhviewer.domain.model

sealed interface AuthEvent {
    object SessionExpired : AuthEvent
}
