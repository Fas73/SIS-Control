package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import kotlinx.coroutines.launch

class GuardInstallationsViewModel(
    private val getInstallationsUseCase: GetInstallationsUseCase
) : ViewModel() {

    private val _state = mutableStateOf(GuardInstallationsState())
    val state: State<GuardInstallationsState> = _state

    init {
        loadInstallations()
    }

    fun loadInstallations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getInstallationsUseCase()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        installations = list,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar instalaciones"
                    )
                }
        }
    }
}

data class GuardInstallationsState(
    val installations: List<InstallationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
