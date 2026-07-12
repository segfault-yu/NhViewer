package com.example.nhviewer.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.usecase.LoginUseCase
import com.example.nhviewer.domain.usecase.RegisterUseCase
import com.example.nhviewer.domain.usecase.ResetPasswordUseCase
import com.example.nhviewer.domain.usecase.ResetPasswordConfirmUseCase
import com.example.nhviewer.domain.usecase.GetPowChallengeUseCase
import com.example.nhviewer.domain.usecase.GetCaptchaConfigUseCase
import com.example.nhviewer.util.PowSolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val resetPasswordConfirmUseCase: ResetPasswordConfirmUseCase,
    private val getPowChallengeUseCase: GetPowChallengeUseCase,
    private val getCaptchaConfigUseCase: GetCaptchaConfigUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _powStatus = MutableStateFlow("Idle")
    val powStatus: StateFlow<String> = _powStatus.asStateFlow()

    private val _captchaSiteKey = MutableStateFlow<String?>(null)
    val captchaSiteKey: StateFlow<String?> = _captchaSiteKey.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    private var pendingAction: PendingAuthAction? = null

    // 保留 challenge 原文和解算出的 nonce，提交时一并回传给服务端
    private var powChallenge: String = ""
    private var powNonce: String = ""

    sealed interface AuthUiEvent {
        data object Success : AuthUiEvent
        data class Error(val message: String) : AuthUiEvent
        data class Message(val message: String) : AuthUiEvent
    }

    private sealed interface PendingAuthAction {
        data class Login(val username: String, val password: String) : PendingAuthAction
        data class Register(val username: String, val email: String, val password: String) : PendingAuthAction
        data class ResetPassword(val usernameOrEmail: String) : PendingAuthAction
        data class ConfirmReset(val token: String, val newPw: String) : PendingAuthAction
    }

    fun startLogin(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            emitError("账号和密码不能为空")
            return
        }
        pendingAction = PendingAuthAction.Login(username, password)
        executeFlow("login")
    }

    fun startRegister(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            emitError("所有字段均不能为空")
            return
        }
        pendingAction = PendingAuthAction.Register(username, email, password)
        executeFlow("register")
    }

    fun startResetPassword(usernameOrEmail: String) {
        if (usernameOrEmail.isBlank()) {
            emitError("账号或邮箱不能为空")
            return
        }
        pendingAction = PendingAuthAction.ResetPassword(usernameOrEmail)
        executeFlow("reset")
    }

    fun startConfirmReset(token: String, newPw: String) {
        if (token.isBlank() || newPw.isBlank()) {
            emitError("重置凭证与新密码不能为空")
            return
        }
        pendingAction = PendingAuthAction.ConfirmReset(token, newPw)
        executeFlow("reset")
    }

    private fun executeFlow(action: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _powStatus.value = "获取 PoW 挑战配置..."

            getPowChallengeUseCase(action).onSuccess { powDto ->
                _powStatus.value = "解算 PoW 工作量碰撞中..."
                // 保存 challenge 原文，服务端需要在提交时回传它
                powChallenge = powDto.challenge
                powNonce = PowSolver.solve(powDto.challenge, powDto.difficulty)

                _powStatus.value = "获取验证码安全校验项..."
                getCaptchaConfigUseCase().onSuccess { captchaDto ->
                    _powStatus.value = "等待进行人机验证..."
                    _captchaSiteKey.value = captchaDto.siteKey
                }.onFailure {
                    _isLoading.value = false
                    _powStatus.value = "Idle"
                    emitError("获取 CAPTCHA 配置失败: ${it.localizedMessage}")
                }
            }.onFailure {
                _isLoading.value = false
                _powStatus.value = "Idle"
                emitError("获取 PoW 挑战失败: ${it.localizedMessage}")
            }
        }
    }

    fun onCaptchaSuccess(captchaResponse: String) {
        _captchaSiteKey.value = null
        _powStatus.value = "正在提交凭据..."

        viewModelScope.launch {
            val result = when (val action = pendingAction) {
                is PendingAuthAction.Login -> {
                    loginUseCase(
                        action.username, action.password,
                        powChallenge, powNonce, captchaResponse
                    ).map { Unit }
                }
                is PendingAuthAction.Register -> {
                    registerUseCase(
                        action.username, action.email, action.password,
                        powChallenge, powNonce, captchaResponse
                    ).map { Unit }
                }
                is PendingAuthAction.ResetPassword -> {
                    resetPasswordUseCase(
                        action.usernameOrEmail,
                        powChallenge, powNonce, captchaResponse
                    )
                }
                is PendingAuthAction.ConfirmReset -> {
                    resetPasswordConfirmUseCase(
                        action.token, action.newPw,
                        powChallenge, powNonce, captchaResponse
                    )
                }
                null -> Result.failure(Exception("无可执行的操作"))
            }

            _isLoading.value = false
            _powStatus.value = "Idle"
            pendingAction = null
            powChallenge = ""
            powNonce = ""

            result.onSuccess {
                _uiEvent.emit(AuthUiEvent.Success)
            }.onFailure {
                emitError(it.localizedMessage ?: "提交验证失败")
            }
        }
    }

    fun cancelCaptcha() {
        _captchaSiteKey.value = null
        _isLoading.value = false
        _powStatus.value = "Idle"
        pendingAction = null
        powChallenge = ""
        powNonce = ""
    }

    private fun emitError(msg: String) {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.Error(msg))
        }
    }
}
