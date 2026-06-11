package com.siscontrol.mobile.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val token: String, val role: String, val userId: Long, val fullName: String, val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val authRepository: com.siscontrol.mobile.domain.repository.AuthRepository,
    private val sessionManager: com.siscontrol.mobile.di.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetLoginState() {
        _uiState.value = LoginUiState.Idle
    }

    private val _isUsernameVerified = MutableStateFlow(false)
    val isUsernameVerified: StateFlow<Boolean> = _isUsernameVerified.asStateFlow()

    private val _isVerifyingUsername = MutableStateFlow(false)
    val isVerifyingUsername: StateFlow<Boolean> = _isVerifyingUsername.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _isQuickLoginMode = MutableStateFlow(false)
    val isQuickLoginMode: StateFlow<Boolean> = _isQuickLoginMode.asStateFlow()

    private val _lastUsername = MutableStateFlow<String?>("test")
    val lastUsername: StateFlow<String?> = _lastUsername.asStateFlow()

    private val _lastFullName = MutableStateFlow<String?>("Test User")
    val lastFullName: StateFlow<String?> = _lastFullName.asStateFlow()

    init {
        viewModelScope.launch {
            val lastUser = sessionManager.lastUsernameFlow.first()
            val lastName = sessionManager.lastFullNameFlow.first()
            
            if (!lastUser.isNullOrBlank()) {
                _lastUsername.value = lastUser
                _lastFullName.value = lastName
                _isQuickLoginMode.value = true
                _isUsernameVerified.value = true // Pre-verificado para QuickLogin
            } else {
                _lastUsername.value = ""
                _lastFullName.value = ""
            }
        }
    }

    fun switchToNormalLogin() {
        _isQuickLoginMode.value = false
        _isUsernameVerified.value = false
        _lastUsername.value = ""
        viewModelScope.launch {
            sessionManager.clearLastUser()
        }
    }

    fun verifyUsername(username: String) {
        if (username.isBlank()) {
            _usernameError.value = "Por favor, ingresa un usuario o correo."
            return
        }

        viewModelScope.launch {
            _isVerifyingUsername.value = true
            _usernameError.value = null
            
            val result = authRepository.checkUsername(username)
            result.fold(
                onSuccess = { exists ->
                    if (exists) {
                        _isUsernameVerified.value = true
                    } else {
                        _isUsernameVerified.value = false
                        _usernameError.value = "El usuario o correo no se encuentra registrado."
                    }
                },
                onFailure = { error ->
                    _isUsernameVerified.value = false
                    _usernameError.value = error.message ?: "Error al verificar usuario"
                }
            )
            _isVerifyingUsername.value = false
        }
    }

    fun resetUsernameVerification() {
        _isUsernameVerified.value = false
        _usernameError.value = null
    }

    fun performLogin(username: String, password: String) {
        // Evitamos peticiones si ya estamos cargando
        if (_uiState.value is LoginUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            // LLAMADA REAL AL BACKEND
            val result = loginUseCase(username, password)

            result.fold(
                onSuccess = { loginResult ->
                    // Si el backend validó correctamente
                    _uiState.value = LoginUiState.Success(
                        token = loginResult.token,
                        role  = loginResult.role,
                        userId = loginResult.userId,
                        fullName = loginResult.fullName,
                        username = loginResult.username
                    )
                },
                onFailure = { error ->
                    val errorMsg = com.siscontrol.mobile.core.ErrorUtils.getLoginErrorMessage(error)
                    _uiState.value = LoginUiState.Error(errorMsg)
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}