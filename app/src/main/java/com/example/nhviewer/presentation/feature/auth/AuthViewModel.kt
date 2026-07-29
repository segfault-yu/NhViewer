package com.example.nhviewer.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.usecase.LoginUseCase
import com.example.nhviewer.domain.usecase.RegisterUseCase
import com.example.nhviewer.domain.usecase.ResetPasswordUseCase
import com.example.nhviewer.domain.usecase.ResetPasswordConfirmUseCase
import com.example.nhviewer.domain.usecase.GetPowChallengeUseCase
import com.example.nhviewer.domain.usecase.GetCaptchaConfigUseCase
import com.example.nhviewer.util.NetworkErrorParser
import com.example.nhviewer.util.log.AppLogger
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

    sealed interface PrefetchState<out T> {
        object Idle : PrefetchState<Nothing>
        object Loading : PrefetchState<Nothing>
        data class Success<out T>(val data: T) : PrefetchState<T>
        data class Failure(val error: Throwable) : PrefetchState<Nothing>
    }

    // 验证码 site key 是静态配置、可复用，提前拉取没毛；
    // PoW challenge 是一次性、短时效的，不提前拉取解算，在 onCaptchaSuccess 里现取现算
    private val captchaPrefetchState = MutableStateFlow<PrefetchState<String>>(PrefetchState.Idle)

    init {
        prefetchCaptcha()
    }

    private fun prefetchCaptcha() {
        if (captchaPrefetchState.value is PrefetchState.Loading || captchaPrefetchState.value is PrefetchState.Success) return

        viewModelScope.launch {
            captchaPrefetchState.value = PrefetchState.Loading
            getCaptchaConfigUseCase().onSuccess { captchaDto ->
                captchaPrefetchState.value = PrefetchState.Success(captchaDto.siteKey)
            }.onFailure {
                AppLogger.w("Auth", "验证码配置获取失败")
                captchaPrefetchState.value = PrefetchState.Failure(it)
            }
        }
    }

    private fun resetCaptchaPrefetch() {
        captchaPrefetchState.value = PrefetchState.Idle
        prefetchCaptcha()
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

    // 展示给用户；PoW challenge 留到通过验证码后再现取现算
    private fun executeFlow(action: String) {
        viewModelScope.launch {
            if (captchaPrefetchState.value is PrefetchState.Failure || captchaPrefetchState.value is PrefetchState.Idle) {
                prefetchCaptcha()
            }

            val isAlreadySuccess = captchaPrefetchState.value is PrefetchState.Success

            if (!isAlreadySuccess) {
                _isLoading.value = true
                _powStatus.value = "正在准备安全验证..."
            }

            try {
                val captchaSiteKeyVal = waitForCaptchaSuccess()
                _isLoading.value = false
                _captchaSiteKey.value = captchaSiteKeyVal
            } catch (e: Exception) {
                AppLogger.w("Auth", "安全校验准备失败 (action=$action)", e)
                _isLoading.value = false
                _powStatus.value = "Idle"
                emitError("准备安全校验失败: ${NetworkErrorParser.parse(e)}")
                resetCaptchaPrefetch()
            }
        }
    }

    fun onCaptchaSuccess(captchaResponse: String) {
        _captchaSiteKey.value = null
        _powStatus.value = "正在验证安全性..."
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

            // 紧贴提交前才请求并解算 PoW challenge，避免填表单、过验证码耗费的时间导致 challenge 过期
            val result: Result<Unit> = if (action == null || actionStr == null) {
                Result.failure(Exception("无可执行的操作"))
            } else {
                val powResult = getPowChallengeUseCase(actionStr)
                val powDto = powResult.getOrNull()
                if (powDto == null) {
                    Result.failure(powResult.exceptionOrNull() ?: Exception("PoW 挑战获取失败"))
                } else {
                    val nonce = PowSolver.solve(powDto.challenge, powDto.difficulty)
                    _powStatus.value = "正在提交凭据..."
                    when (action) {
                        is PendingAuthAction.Login -> loginUseCase(
                            action.username, action.password, powDto.challenge, nonce, captchaResponse
                        ).map { Unit }
                        is PendingAuthAction.Register -> registerUseCase(
                            action.username, action.email, action.password, powDto.challenge, nonce, captchaResponse
                        ).map { Unit }
                        is PendingAuthAction.ResetPassword -> resetPasswordUseCase(
                            action.usernameOrEmail, powDto.challenge, nonce, captchaResponse
                        )
                        is PendingAuthAction.ConfirmReset -> resetPasswordConfirmUseCase(
                            action.token, action.newPw, powDto.challenge, nonce, captchaResponse
                        )
                    }
                }
            }

            _isLoading.value = false
            _powStatus.value = "Idle"
            pendingAction = null
            resetCaptchaPrefetch()

            // 只记操作类型与结果，用户名、密码、令牌不入日志
            result.onSuccess {
                AppLogger.i("Auth", "认证操作成功 (action=$actionStr)")
                _uiEvent.emit(AuthUiEvent.Success)
            }.onFailure {
                AppLogger.w("Auth", "认证操作失败 (action=$actionStr)")
                emitError(NetworkErrorParser.parse(it))
            }
        }
    }

    fun cancelCaptcha() {
        _captchaSiteKey.value = null
        _isLoading.value = false
        _powStatus.value = "Idle"
        pendingAction = null
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
