package com.example.nhviewer.presentation.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.UserSession
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.usecase.GetSessionsUseCase
import com.example.nhviewer.domain.usecase.LogoutUseCase
import com.example.nhviewer.domain.usecase.RevokeSessionUseCase
import com.example.nhviewer.util.NetworkErrorParser
import com.example.nhviewer.util.log.AppLogger
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

    fun loadSessions(forceRefresh: Boolean = false) {
        if (!forceRefresh && _sessions.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            getSessionsUseCase().onSuccess {
                _sessions.value = it
                _errorMessage.value = null
            }.onFailure {
                AppLogger.w("Profile", "会话列表加载失败")
                _errorMessage.value = NetworkErrorParser.parse(it)
            }
            _isLoading.value = false
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            revokeSessionUseCase(sessionId).onSuccess {
                AppLogger.i("Profile", "会话已注销")
                loadSessions()
            }.onFailure {
                AppLogger.w("Profile", "注销单个会话失败")
                _errorMessage.value = NetworkErrorParser.parse(it)
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            logoutUseCase().onFailure {
                AppLogger.w("Profile", "退出登录失败")
                _errorMessage.value = NetworkErrorParser.parse(it)
            }
            _isLoading.value = false
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.logoutAll().onSuccess {
                AppLogger.i("Profile", "已注销全部会话")
                _sessions.value = emptyList()
            }.onFailure {
                AppLogger.w("Profile", "注销全部会话失败")
                _errorMessage.value = NetworkErrorParser.parse(it)
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
