package com.siscontrol.mobile.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.GetUserByIdUseCase
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = sessionManager.getUserIdSync()
            if (userId == null || userId == 0L) {
                _state.value = _state.value.copy(error = "No se pudo identificar la sesión.")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)
            getUserByIdUseCase(userId)
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        user = user,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar perfil"
                    )
                }
        }
    }
}

data class ProfileState(
    val user: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
