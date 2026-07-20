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
import androidx.annotation.StringRes
import com.example.nhviewer.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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

    sealed interface PrefetchState<out T> {
        object Idle : PrefetchState<Nothing>
        object Loading : PrefetchState<Nothing>
        data class Success<out T>(val data: T) : PrefetchState<T>
        data class Failure(val error: Throwable) : PrefetchState<Nothing>
    }

    data class PrefetchedPow(
        val challenge: String,
        val nonce: String
    )

    private val powPrefetchMap = mapOf(
        "login" to MutableStateFlow<PrefetchState<PrefetchedPow>>(PrefetchState.Idle),
        "register" to MutableStateFlow<PrefetchState<PrefetchedPow>>(PrefetchState.Idle),
        "reset" to MutableStateFlow<PrefetchState<PrefetchedPow>>(PrefetchState.Idle)
    )

    private val captchaPrefetchState = MutableStateFlow<PrefetchState<String>>(PrefetchState.Idle)

    init {
        prefetchSecurityConfigs()
    }

    fun prefetchSecurityConfigs() {
        prefetchPow("login")
        prefetchPow("register")
        prefetchPow("reset")
        prefetchCaptcha()
    }

    private fun prefetchPow(action: String) {
        val flow = powPrefetchMap[action] ?: return
        if (flow.value is PrefetchState.Loading || flow.value is PrefetchState.Success) return

        viewModelScope.launch {
            flow.value = PrefetchState.Loading
            getPowChallengeUseCase(action).onSuccess { powDto ->
                try {
                    val nonce = PowSolver.solve(powDto.challenge, powDto.difficulty)
                    flow.value = PrefetchState.Success(PrefetchedPow(powDto.challenge, nonce))
                } catch (e: Exception) {
                    flow.value = PrefetchState.Failure(e)
                }
            }.onFailure {
                flow.value = PrefetchState.Failure(it)
            }
        }
    }

    private fun prefetchCaptcha() {
        if (captchaPrefetchState.value is PrefetchState.Loading || captchaPrefetchState.value is PrefetchState.Success) return

        viewModelScope.launch {
            captchaPrefetchState.value = PrefetchState.Loading
            getCaptchaConfigUseCase().onSuccess { captchaDto ->
                captchaPrefetchState.value = PrefetchState.Success(captchaDto.siteKey)
            }.onFailure {
                captchaPrefetchState.value = PrefetchState.Failure(it)
            }
        }
    }

    private fun resetPrefetch(action: String) {
        powPrefetchMap[action]?.value = PrefetchState.Idle
        prefetchPow(action)
    }

    private fun resetCaptchaPrefetch() {
        captchaPrefetchState.value = PrefetchState.Idle
        prefetchCaptcha()
    }

    private suspend fun waitForPowSuccess(action: String): PrefetchedPow {
        val flow = powPrefetchMap[action] ?: throw Exception("未知的操作类型")
        val resultState = flow.filter { it is PrefetchState.Success || it is PrefetchState.Failure }.first()
        if (resultState is PrefetchState.Failure) {
            throw resultState.error
        }
        return (resultState as PrefetchState.Success).data
    }

    private suspend fun waitForCaptchaSuccess(): String {
        val resultState = captchaPrefetchState.filter { it is PrefetchState.Success || it is PrefetchState.Failure }.first()
        if (resultState is PrefetchState.Failure) {
            throw resultState.error
        }
        return (resultState as PrefetchState.Success).data
    }

    sealed interface AuthUiEvent {
        data object Success : AuthUiEvent
        data class Error(val message: String) : AuthUiEvent
        data class ErrorRes(@StringRes val resId: Int) : AuthUiEvent
        data class Message(val message: String) : AuthUiEvent
        data class MessageRes(@StringRes val resId: Int) : AuthUiEvent
    }

    private sealed interface PendingAuthAction {
        data class Login(val username: String, val password: String) : PendingAuthAction
        data class Register(val username: String, val email: String, val password: String) : PendingAuthAction
        data class ResetPassword(val usernameOrEmail: String) : PendingAuthAction
        data class ConfirmReset(val token: String, val newPw: String) : PendingAuthAction
    }

    fun startLogin(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            emitErrorRes(R.string.auth_err_username_pwd_empty)
            return
        }
        pendingAction = PendingAuthAction.Login(username, password)
        executeFlow("login")
    }

    fun startRegister(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            emitErrorRes(R.string.auth_err_all_fields_required)
            return
        }
        pendingAction = PendingAuthAction.Register(username, email, password)
        executeFlow("register")
    }

    fun startResetPassword(usernameOrEmail: String) {
        if (usernameOrEmail.isBlank()) {
            emitErrorRes(R.string.auth_err_account_email_empty)
            return
        }
        pendingAction = PendingAuthAction.ResetPassword(usernameOrEmail)
        executeFlow("reset")
    }

    fun startConfirmReset(token: String, newPw: String) {
        if (token.isBlank() || newPw.isBlank()) {
            emitErrorRes(R.string.auth_err_token_pwd_empty)
            return
        }
        pendingAction = PendingAuthAction.ConfirmReset(token, newPw)
        executeFlow("reset")
    }

    private fun executeFlow(action: String) {
        viewModelScope.launch {
            val powFlow = powPrefetchMap[action] ?: return@launch
            val captchaFlow = captchaPrefetchState

            if (powFlow.value is PrefetchState.Failure || powFlow.value is PrefetchState.Idle) {
                prefetchPow(action)
            }
            if (captchaFlow.value is PrefetchState.Failure || captchaFlow.value is PrefetchState.Idle) {
                prefetchCaptcha()
            }

            val isAlreadySuccess = powFlow.value is PrefetchState.Success && captchaFlow.value is PrefetchState.Success
            
            if (!isAlreadySuccess) {
                _isLoading.value = true
                _powStatus.value = "正在准备安全验证..."
            }

            try {
                val powData = waitForPowSuccess(action)
                val captchaSiteKeyVal = waitForCaptchaSuccess()

                powChallenge = powData.challenge
                powNonce = powData.nonce

                _isLoading.value = false
                _captchaSiteKey.value = captchaSiteKeyVal
            } catch (e: Exception) {
                _isLoading.value = false
                _powStatus.value = "Idle"
                emitError("准备安全校验失败: ${e.localizedMessage}")
                resetPrefetch(action)
            }
        }
    }

    fun onCaptchaSuccess(captchaResponse: String) {
        _captchaSiteKey.value = null
        _powStatus.value = "正在提交凭据..."
        _isLoading.value = true

        viewModelScope.launch {
            val action = pendingAction
            val actionStr = when (action) {
                is PendingAuthAction.Login -> "login"
                is PendingAuthAction.Register -> "register"
                is PendingAuthAction.ResetPassword -> "reset"
                is PendingAuthAction.ConfirmReset -> "reset"
                null -> null
            }

            val result = when (action) {
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

            if (actionStr != null) {
                resetPrefetch(actionStr)
            }
            resetCaptchaPrefetch()

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
        val action = pendingAction
        pendingAction = null
        powChallenge = ""
        powNonce = ""

        if (action != null) {
            val actionStr = when (action) {
                is PendingAuthAction.Login -> "login"
                is PendingAuthAction.Register -> "register"
                is PendingAuthAction.ResetPassword -> "reset"
                is PendingAuthAction.ConfirmReset -> "reset"
            }
            resetPrefetch(actionStr)
        }
    }

    private fun emitError(msg: String) {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.Error(msg))
        }
    }

    private fun emitErrorRes(@StringRes resId: Int) {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.ErrorRes(resId))
        }
    }
}
