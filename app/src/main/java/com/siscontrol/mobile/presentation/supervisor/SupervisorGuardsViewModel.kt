package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.GetPersonnelUseCase
import com.siscontrol.mobile.domain.usecase.ToggleUserStatusUseCase
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

class SupervisorGuardsViewModel(
    private val getPersonnelUseCase: GetPersonnelUseCase,
    private val toggleUserStatusUseCase: ToggleUserStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(SupervisorGuardsState())
    val state: State<SupervisorGuardsState> = _state

    init {
        loadGuards()
    }

    fun loadGuards() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getPersonnelUseCase()
                .onSuccess { list ->
                    val onlyGuards = list.filter { it.role == "GUARD" || it.role == "GUARDIA" }
                    _state.value = _state.value.copy(
                        guards = onlyGuards,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar guardias"
                    )
                }
        }
    }

    fun toggleGuardStatus(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            val editorId = getEditorId()
            
            toggleUserStatusUseCase(userId, editorId)
                .onSuccess {
                    _state.value = _state.value.copy(isActionLoading = false)
                    loadGuards()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isActionLoading = false, error = e.message)
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

data class SupervisorGuardsState(
    val guards: List<UserResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null
)
