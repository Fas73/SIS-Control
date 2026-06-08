package com.siscontrol.mobile.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
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
                    _state.value = _state.value.copy(user = user, isLoading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Error al cargar perfil")
                }
        }
    }

    // CARGA INMEDIATA DE FOTO DE PERFIL
    fun uploadProfilePicture(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)

            android.util.Log.d("PROFILE_VM", "Iniciando subida de imagen a Firebase: $uri")
            
            FirebaseStorageManager.uploadImage(context, uri, "perfiles")
                .onSuccess { remoteUrl: String ->
                    android.util.Log.d("PROFILE_VM", "Firebase OK! Link obtenido: $remoteUrl")
                    
                    // Usamos el nuevo endpoint dedicado
                    updateProfileImageUseCase(user.id ?: 0L, remoteUrl)
                        .onSuccess { updatedUser: UserResponseDto ->
                            android.util.Log.d("PROFILE_VM", "Backend OK! Nueva URL en perfil: ${updatedUser.imageUrl}")
                            _state.value = _state.value.copy(user = updatedUser, isActionLoading = false)
                            onResult(true, "Foto de perfil actualizada con éxito")
                        }
                        .onFailure { e: Throwable ->
                            android.util.Log.e("PROFILE_VM", "Backend FAIL: ${e.message}")
                            _state.value = _state.value.copy(isActionLoading = false)
                            onResult(false, "Error al registrar en servidor: ${e.message}")
                        }
                }
                .onFailure { e: Throwable ->
                    android.util.Log.e("PROFILE_VM", "Firebase FAIL: ${e.message}")
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(false, "Error en Firebase: ${e.message}")
                }
        }
    }

    // CARGA DE FOTO RECORTADA (Google Style)
    fun uploadCroppedProfilePicture(bitmap: android.graphics.Bitmap, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)

            FirebaseStorageManager.uploadBitmap(bitmap, "perfiles")
                .onSuccess { remoteUrl: String ->
                    // Usamos el nuevo endpoint dedicado para actualizar solo la foto
                    updateProfileImageUseCase(user.id ?: 0L, remoteUrl)
                        .onSuccess { updatedUser: UserResponseDto ->
                            _state.value = _state.value.copy(user = updatedUser, isActionLoading = false)
                            onResult(true, "Foto de perfil actualizada")
                        }
                        .onFailure { e: Throwable ->
                            _state.value = _state.value.copy(isActionLoading = false)
                            onResult(false, "Error al guardar en BD: ${e.message}")
                        }
                }
                .onFailure { e: Throwable ->
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(false, "Error en Firebase: ${e.message}")
                }
        }
    }

    fun updateProfile(fullName: String, username: String, phoneNumber: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)

            val request = ProfileUpdateRequest(
                fullName = fullName,
                username = username,
                phoneNumber = phoneNumber,
                imageUrl = user.imageUrl
            )

            updateProfileDataUseCase(user.id ?: 0L, request)
                .onSuccess { updatedUser: UserResponseDto ->
                    _state.value = _state.value.copy(user = updatedUser, isActionLoading = false)
                    onResult(true, "Perfil actualizado")
                }
                .onFailure { e: Throwable ->
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(false, e.message ?: "Error al actualizar")
                }
        }
    }

    fun changePassword(currentPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.user ?: return@launch
            _state.value = _state.value.copy(isActionLoading = true)
            changeMyPasswordUseCase(user.id ?: 0L, ChangePasswordRequest(currentPass, newPass))
                .onSuccess { _: Unit ->
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(true, "Clave actualizada")
                }
                .onFailure { e: Throwable ->
                    _state.value = _state.value.copy(isActionLoading = false)
                    onResult(false, "Clave actual incorrecta")
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearActiveSession()
            sessionManager.clearSession()
            onSuccess()
        }
    }
}

data class ProfileState(
    val user: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null
)
