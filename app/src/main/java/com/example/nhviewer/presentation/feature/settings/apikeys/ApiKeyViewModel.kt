package com.example.nhviewer.presentation.feature.settings.apikeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.ApiKey
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.usecase.CreateApiKeyUseCase
import com.example.nhviewer.domain.usecase.GetApiKeysUseCase
import com.example.nhviewer.domain.usecase.RevokeApiKeyUseCase
import com.example.nhviewer.util.PowSolver
import com.example.nhviewer.util.log.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.example.nhviewer.R

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val getApiKeysUseCase: GetApiKeysUseCase,
    private val createApiKeyUseCase: CreateApiKeyUseCase,
    private val revokeApiKeyUseCase: RevokeApiKeyUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _apiKeys = MutableStateFlow<List<ApiKey>>(emptyList())
    val apiKeys: StateFlow<List<ApiKey>> = _apiKeys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessageRes = MutableStateFlow<Int?>(null)
    val errorMessageRes: StateFlow<Int?> = _errorMessageRes.asStateFlow()

    private val _powStatus = MutableStateFlow("Idle")
    val powStatus: StateFlow<String> = _powStatus.asStateFlow()

    private val _captchaSiteKey = MutableStateFlow<String?>(null)
    val captchaSiteKey: StateFlow<String?> = _captchaSiteKey.asStateFlow()

    private val _newlyCreatedApiKey = MutableStateFlow<ApiKey?>(null)
    val newlyCreatedApiKey: StateFlow<ApiKey?> = _newlyCreatedApiKey.asStateFlow()

    private var pendingKeyName: String = ""
    private var powChallenge: String = ""
    private var powNonce: String = ""

    init {
        loadApiKeys()
    }

    fun loadApiKeys() {
        viewModelScope.launch {
            _isLoading.value = true
            getApiKeysUseCase().onSuccess {
                _apiKeys.value = it
                _errorMessageRes.value = null
            }.onFailure {
                AppLogger.w("ApiKey", "API 密钥列表加载失败")
                _errorMessageRes.value = R.string.sessions_error_fetch
            }
            _isLoading.value = false
        }
    }

    fun initiateCreateApiKey(name: String) {
        if (name.isBlank()) {
            _errorMessageRes.value = R.string.apikeys_name_required
            return
        }
        pendingKeyName = name
        viewModelScope.launch {
            _isLoading.value = true
            _powStatus.value = "Preparing..."
            userRepository.getPowChallenge("api_key").onSuccess { powDto ->
                _powStatus.value = "Solving PoW..."
                powChallenge = powDto.challenge
                val solvedNonce = withContext(Dispatchers.Default) {
                    PowSolver.solve(powDto.challenge, powDto.difficulty)
                }
                powNonce = solvedNonce

                _powStatus.value = "Fetching Captcha..."
                userRepository.getCaptchaConfig().onSuccess { captchaDto ->
                    _powStatus.value = "Waiting Captcha..."
                    _captchaSiteKey.value = captchaDto.siteKey
                }.onFailure {
                    AppLogger.w("ApiKey", "创建密钥前的验证码配置获取失败")
                    _powStatus.value = "Idle"
                    _errorMessageRes.value = R.string.common_unknown_error
                    _isLoading.value = false
                }
            }.onFailure {
                AppLogger.w("ApiKey", "创建密钥前的 PoW 挑战获取失败")
                _powStatus.value = "Idle"
                _errorMessageRes.value = R.string.common_unknown_error
                _isLoading.value = false
            }
        }
    }

    fun onCaptchaSuccess(captchaResponse: String) {
        _captchaSiteKey.value = null
        _powStatus.value = "Generating..."
        viewModelScope.launch {
            // 密钥明文不入日志
            createApiKeyUseCase(pendingKeyName, powChallenge, powNonce, captchaResponse).onSuccess { apiKey ->
                AppLogger.i("ApiKey", "API 密钥创建成功")
                _newlyCreatedApiKey.value = apiKey
                loadApiKeys()
                clearCreationState()
            }.onFailure {
                AppLogger.w("ApiKey", "API 密钥创建失败")
                _errorMessageRes.value = R.string.common_unknown_error
            }
            _powStatus.value = "Idle"
            _isLoading.value = false
        }
    }

    fun cancelCaptcha() {
        _captchaSiteKey.value = null
        _powStatus.value = "Idle"
        _isLoading.value = false
        clearCreationState()
    }

    fun revokeApiKey(keyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            revokeApiKeyUseCase(keyId).onSuccess {
                AppLogger.i("ApiKey", "API 密钥已吊销")
                loadApiKeys()
            }.onFailure {
                AppLogger.w("ApiKey", "API 密钥吊销失败")
                _errorMessageRes.value = R.string.sessions_error_revoke
            }
            _isLoading.value = false
        }
    }

    fun dismissNewlyCreatedKey() {
        _newlyCreatedApiKey.value = null
    }

    fun clearError() {
        _errorMessageRes.value = null
    }

    private fun clearCreationState() {
        pendingKeyName = ""
        powChallenge = ""
        powNonce = ""
    }
}
