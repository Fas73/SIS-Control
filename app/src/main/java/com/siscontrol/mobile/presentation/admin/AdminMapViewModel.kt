package com.siscontrol.mobile.presentation.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.*
import kotlinx.coroutines.launch

class AdminMapViewModel(
    private val getAdminDashboardUseCase: GetAdminDashboardUseCase
) : ViewModel() {

    private val _state = mutableStateOf(AdminMapState())
    val state: State<AdminMapState> = _state

    init {
        loadMapData()
    }

    fun loadMapData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getAdminDashboardUseCase()
                .onSuccess { data ->
                    // Transformamos las jornadas activas en pins del mapa
                    val activeGuards = data.activeShiftsList.map { shift ->
                        // Verificamos si este guardia tiene una ronda activa en la lista unificada
                        val hasActiveRound = data.activeRoundsList.any { it.id == shift.id }

                        GuardMapPin(
                            guardId = shift.id,
                            guardName = shift.guardName,
                            installationName = shift.location,
                            latitude = -33.4489, // El backend debería proveer lat/lon en el DTO para precisión total
                            longitude = -70.6693,
                            status = if (hasActiveRound) "En Ronda" else "Activo"
                        )
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        activeGuards = activeGuards
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = com.siscontrol.mobile.core.ErrorUtils.parse(e)
                    )
                }
        }
    }
}

data class AdminMapState(
    val isLoading: Boolean = false,
    val activeGuards: List<GuardMapPin> = emptyList(),
    val error: String? = null
)

data class GuardMapPin(
    val guardId: Long,
    val guardName: String,
    val installationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String // "En Ronda", "Activo"
)
