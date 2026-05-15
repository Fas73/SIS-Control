package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import com.siscontrol.mobile.domain.usecase.GetPersonnelUseCase
import kotlinx.coroutines.launch

class SupervisorGuardsViewModel(
    private val getPersonnelUseCase: GetPersonnelUseCase
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
                    // El supervisor solo ve GUARD o GUARDIA
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
}

data class SupervisorGuardsState(
    val guards: List<UserResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
