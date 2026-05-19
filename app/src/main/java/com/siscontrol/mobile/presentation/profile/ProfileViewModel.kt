package com.siscontrol.mobile.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.AttendanceRequest
import com.siscontrol.mobile.data.remote.dto.ChangePasswordRequest
import com.siscontrol.mobile.data.remote.dto.ProfileUpdateRequest
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val checkOutUseCase: CheckOutUseCase,
    private val updateProfileDataUseCase: UpdateProfileDataUseCase,
    private val changeMyPasswordUseCase: ChangeMyPasswordUseCase,
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

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userId = sessionManager.getUserIdSync() ?: 0L
            val installationId = sessionManager.getActiveInstallationIdSync() ?: 0L
            
            if (userId != 0L && installationId != 0L) {
                // Registrar salida antes de cerrar sesión
                checkOutUseCase(
                    AttendanceRequest(
                        userId = userId,
                        installationId = installationId,
                        latitude = -33.3616, // Usamos las mismas de prueba
                        longitude = -70.7304
                    )
                )
            }
            // Borrar también la sesión de ronda activa al cerrar sesión manual
            sessionManager.clearActiveSession()
            sessionManager.clearSession()
            onSuccess()
        }
    }

    fun updateProfile(fullName: String, username: String, phoneNumber: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)

            val request = ProfileUpdateRequest(
                fullName = fullName,
                username = username,
                phoneNumber = phoneNumber
            )

            updateProfileDataUseCase(user.id, request)
                .onSuccess { updatedUser ->
                    // Actualizamos localmente en DataStore y Room para reflejar cambios en toda la App
                    sessionManager.saveSession(
                        token = sessionManager.getTokenSync() ?: "",
                        role = updatedUser.role,
                        userId = updatedUser.id,
                        fullName = updatedUser.fullName
                    )
                    
                    com.siscontrol.mobile.di.AppModule.getDatabase().userSessionDao().insertSession(
                        com.siscontrol.mobile.data.local.entities.UserSessionEntity(
                            id = updatedUser.id,
                            username = updatedUser.username,
                            fullName = updatedUser.fullName,
                            role = updatedUser.role,
                            status = updatedUser.status.toString()
                        )
                    )

                    _state.value = _state.value.copy(user = updatedUser, isActionLoading = false)
                    onResult(true, "Perfil actualizado correctamente")
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(false, e.message ?: "Error al actualizar perfil")
                }
        }
    }

    fun changePassword(currentPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)

            val request = ChangePasswordRequest(
                currentPassword = currentPass,
                newPassword = newPass
            )

            changeMyPasswordUseCase(user.id, request)
                .onSuccess {
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(true, "Contraseña actualizada correctamente")
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(false, "La contraseña actual es incorrecta o hubo un error en el servidor")
                }
        }
    }
}

data class ProfileState(
    val user: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null
)
