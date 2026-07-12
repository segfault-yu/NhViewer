package com.example.nhviewer.domain.model

sealed interface AuthState {
    data object LoggedOut : AuthState
    data class LoggedIn(val user: User) : AuthState
}
