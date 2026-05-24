package com.siscontrol.mobile.presentation.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.model.Checkpoint
import com.siscontrol.mobile.domain.usecase.GetCheckpointsUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import kotlinx.coroutines.launch

data class CheckpointsState(
    val isLoading: Boolean = false,
    val checkpoints: List<Checkpoint> = emptyList(),
    val filteredCheckpoints: List<Checkpoint> = emptyList(),
    val error: String? = null,
    val selectedInstallationName: String? = null
)

class AdminCheckpointsViewModel(
    private val getCheckpointsUseCase: GetCheckpointsUseCase,
    private val getInstallationsUseCase: GetInstallationsUseCase
) : ViewModel() {

    var state by mutableStateOf(CheckpointsState())
        private set

    /**
     * Carga los checkpoints.
     * @param installationId Si es null, intenta cargar todos de forma iterativa.
     * Si viene un ID, carga solo los de esa instalación.
     */
    fun loadCheckpoints(installationId: Long? = null) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            if (installationId != null) {
                // CASO: Filtrado por una instalación específica (Botón "Ver Puntos")
                getCheckpointsUseCase(installationId).onSuccess { list ->
                    state = state.copy(
                        isLoading = false,
                        checkpoints = list,
                        filteredCheckpoints = list
                    )
                }.onFailure { e ->
                    state = state.copy(isLoading = false, error = e.message ?: "Error al cargar puntos")
                }
            } else {
                // CASO: Carga global (Botón General de Checkpoints)
                loadAllCheckpointsIteratively()
            }
        }
    }

    private suspend fun loadAllCheckpointsIteratively() {
        getInstallationsUseCase().onSuccess { installations ->
            val allCheckpoints = mutableListOf<Checkpoint>()

            installations.forEach { inst ->
                inst.id?.let { id ->
                    getCheckpointsUseCase(id).onSuccess { points ->
                        allCheckpoints.addAll(points)
                    }
                }
            }

            state = state.copy(
                isLoading = false,
                checkpoints = allCheckpoints,
                filteredCheckpoints = allCheckpoints
            )
        }.onFailure { e ->
            state = state.copy(isLoading = false, error = e.message)
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = if (query.isBlank()) {
            state.checkpoints
        } else {
            state.checkpoints.filter {
                (it.name ?: "").contains(query, ignoreCase = true) ||
                        (it.nfcTagCode ?: "").contains(query, ignoreCase = true)
            }
        }
        state = state.copy(filteredCheckpoints = filtered)
    }
}