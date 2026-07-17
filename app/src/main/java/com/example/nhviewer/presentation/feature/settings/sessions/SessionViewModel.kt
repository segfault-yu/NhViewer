package com.example.nhviewer.presentation.feature.settings.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.UserSession
import com.example.nhviewer.domain.usecase.GetSessionsUseCase
import com.example.nhviewer.domain.usecase.RevokeSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getSessionsUseCase: GetSessionsUseCase,
    private val revokeSessionUseCase: RevokeSessionUseCase
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<UserSession>>(emptyList())
    val sessions: StateFlow<List<UserSession>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            getSessionsUseCase().onSuccess {
                _sessions.value = it
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "获取设备列表失败"
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
                _errorMessage.value = it.localizedMessage ?: "吊销设备会话失败"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
