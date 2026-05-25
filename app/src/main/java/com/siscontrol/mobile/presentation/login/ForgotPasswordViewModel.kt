package com.siscontrol.mobile.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.RecoverAccessUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel para la recuperación de contraseña.
 * Solicita ayuda al Administrador mediante el endpoint seguro del Backend.
 */
class ForgotPasswordViewModel(
    private val recoverAccessUseCase: RecoverAccessUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: State<ForgotPasswordUiState> = _uiState

    fun enviarSolicitudSoporte(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ForgotPasswordUiState.Error("Por favor, ingresa un correo electrónico válido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            
            recoverAccessUseCase(email.trim())
                .onSuccess { mensajeExito ->
                    _uiState.value = ForgotPasswordUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        error.message ?: "Error al procesar la solicitud. Intente más tarde."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }
}
