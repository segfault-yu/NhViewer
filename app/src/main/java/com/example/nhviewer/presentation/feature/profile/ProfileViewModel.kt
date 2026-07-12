package com.example.nhviewer.presentation.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.UserSession
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.usecase.GetSessionsUseCase
import com.example.nhviewer.domain.usecase.LogoutUseCase
import com.example.nhviewer.domain.usecase.RevokeSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val logoutUseCase: LogoutUseCase,
    private val getSessionsUseCase: GetSessionsUseCase,
    private val revokeSessionUseCase: RevokeSessionUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = userRepository.authState

    private val _sessions = MutableStateFlow<List<UserSession>>(emptyList())
    val sessions: StateFlow<List<UserSession>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            authState.collectLatest { state ->
                if (state is AuthState.LoggedIn) {
                    loadSessions()
                } else {
                    _sessions.value = emptyList()
                }
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            getSessionsUseCase().onSuccess {
                _sessions.value = it
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "获取会话列表失败"
            }
            _isLoading.value = false
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            revokeSessionUseCase(sessionId).onSuccess {
                loadSessions()
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "吊销会话失败"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            logoutUseCase().onFailure {
                _errorMessage.value = it.localizedMessage ?: "登出失败"
            }
            _isLoading.value = false
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.logoutAll().onSuccess {
                _sessions.value = emptyList()
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "强制全部登出失败"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
