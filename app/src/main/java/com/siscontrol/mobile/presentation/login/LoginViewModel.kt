package com.siscontrol.mobile.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val token: String, val role: String, val userId: Long, val fullName: String, val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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