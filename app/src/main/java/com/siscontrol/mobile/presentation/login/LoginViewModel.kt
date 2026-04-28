package com.siscontrol.mobile.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.LoginUseCase
import com.siscontrol.mobile.domain.usecase.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Representa los diferentes estados posibles de la UI de la pantalla de Login.
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    /** Autenticación exitosa. Lleva el JWT y el rol del usuario para la navegación. */
    data class Success(val token: String, val role: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel del patrón MVVM destinado a gestionar el estado y la lógica de presentación
 * de la pantalla de inicio de sesión.
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Función invocada desde la Vista (Compose) cuando el usuario pulsa el botón "Ingresar".
     * Lanza una corrutina en el scope del ViewModel para que se cancele automáticamente 
     * si el ViewModel se destruye.
     */
    fun performLogin(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            // Simulación de retardo para feedback visual
            kotlinx.coroutines.delay(800)
            
            // Determinar rol basado en el input para facilitar pruebas
            val role = when {
                username.contains("admin", ignoreCase = true) -> "ADMIN"
                username.contains("sup", ignoreCase = true) -> "SUPERVISOR"
                username.contains("guard", ignoreCase = true) -> "GUARDIA"
                else -> "ADMIN" // Default para pruebas rápidas
            }
            
            // ¡Autenticados exitosamente de forma simulada!
            _uiState.value = LoginUiState.Success(
                token = "mock-jwt-token-for-testing",
                role  = role
            )
        }
    }
}
