package com.siscontrol.mobile.presentation.admin

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

class AdminManagementViewModel(
    private val getPersonnelUseCase: GetPersonnelUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updatePersonnelUseCase: UpdatePersonnelUseCase,
    private val toggleUserStatusUseCase: ToggleUserStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(AdminManagementState())
    val state: State<AdminManagementState> = _state
    
    // Estado para edición individual
    private val _editingUser = mutableStateOf<UserResponseDto?>(null)
    val editingUser: State<UserResponseDto?> = _editingUser

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getPersonnelUseCase()
                .onSuccess { list ->
                    Log.d("AdminVM", "Usuarios cargados: ${list.size}")
                    _state.value = _state.value.copy(
                        users = list,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    Log.e("AdminVM", "Error al cargar usuarios", exception)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(exception)
                    )
                }
        }
    }

    fun getUserById(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true, error = null)
            getUserByIdUseCase(userId)
                .onSuccess { user ->
                    _editingUser.value = user
                    _state.value = _state.value.copy(isActionLoading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isActionLoading = false, 
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun toggleUserStatus(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            val editorId = getEditorId()
            
            toggleUserStatusUseCase(userId, editorId)
                .onSuccess {
                    _state.value = _state.value.copy(isActionLoading = false)
                    loadUsers()
                }
                .onFailure { e ->
                    Log.e("AdminVM", "Error al cambiar estado", e)
                    _state.value = _state.value.copy(isActionLoading = false, error = e.message)
                }
        }
    }

    fun updateUser(userId: Long, request: UserRequestDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true, error = null)
            val editorId = getEditorId()
            
            updatePersonnelUseCase(userId, editorId, request)
                .onSuccess {
                    _state.value = _state.value.copy(isActionLoading = false)
                    loadUsers()
                    onSuccess()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isActionLoading = false, 
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }

    fun updateUserRole(user: UserResponseDto, newRole: String) {
        viewModelScope.launch {
            Log.d("AdminVM", "Cambiando rol de ${user.username} (ID: ${user.id}) a $newRole")
            _state.value = _state.value.copy(isActionLoading = true, error = null)
            val editorId = getEditorId()
            Log.d("AdminVM", "Editor ID (el que realiza el cambio): $editorId")

            val request = UserRequestDto(
                rut = (user.rut ?: "11111111-1").replace(".", ""), // Quitamos puntos si existen
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                password = "pass123",
                phoneNumber = user.phoneNumber ?: "+56900000000",
                role = newRole
            )

            updatePersonnelUseCase(user.id, editorId, request)
                .onSuccess {
                    Log.d("AdminVM", "Respuesta exitosa del servidor. Refrescando lista...")
                    _state.value = _state.value.copy(isActionLoading = false)
                    loadUsers()
                }
                .onFailure { e ->
                    val errorMsg = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    Log.e("AdminVM", "FALLÓ la actualización de rol: $errorMsg", e)
                    _state.value = _state.value.copy(
                        isActionLoading = false, 
                        error = "No se pudo cambiar el rol: $errorMsg"
                    )
                }
        }
    }

    private suspend fun getEditorId(): Long {
        val sessionRoom = com.siscontrol.mobile.di.AppModule.getDatabase().userSessionDao().getSessionSync()
        val roomUserId = sessionRoom?.id ?: 0L
        val dsUserId = sessionManager.getUserIdSync() ?: 0L
        return if (roomUserId > 0) roomUserId else dsUserId
    }
}

data class AdminManagementState(
    val users: List<UserResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null
)
