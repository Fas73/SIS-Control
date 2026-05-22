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
import com.siscontrol.mobile.core.FirebaseStorageManager
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
            // Limpiamos los datos locales
            sessionManager.clearActiveSession()
            sessionManager.clearSession()
            onSuccess()
        }
    }

    fun updateProfile(fullName: String, username: String, phoneNumber: String, imageUri: android.net.Uri? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)

            var remoteUrl = user.imageUrl
            
            // Si hay una nueva imagen, subirla a Firebase
            if (imageUri != null) {
                FirebaseStorageManager.uploadImage(imageUri, "perfiles")
                    .onSuccess { remoteUrl = it }
                    .onFailure { e ->
                        _state.value = _state.value.copy(isActionLoading = false)
                        onResult(false, "Error al subir foto: ${e.message}")
                        return@launch
                    }
            }

            val request = ProfileUpdateRequest(
                fullName = fullName,
                username = username,
                phoneNumber = phoneNumber,
                imageUrl = remoteUrl
            )

            updateProfileDataUseCase(user.id ?: 0L, request)
                .onSuccess { updatedUser ->
                    // Actualizamos localmente
                    sessionManager.saveSession(
                        token = sessionManager.getTokenSync() ?: "",
                        role = updatedUser.role ?: "GUARD",
                        userId = updatedUser.id ?: 0L,
                        fullName = updatedUser.fullName ?: "Usuario"
                    )
                    
                    com.siscontrol.mobile.di.AppModule.getDatabase().userSessionDao().insertSession(
                        com.siscontrol.mobile.data.local.entities.UserSessionEntity(
                            id = updatedUser.id ?: 0L,
                            username = updatedUser.username ?: "",
                            fullName = updatedUser.fullName ?: "Usuario",
                            role = updatedUser.role ?: "GUARD",
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

            changeMyPasswordUseCase(user.id ?: 0L, request)
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
