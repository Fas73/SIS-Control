package com.siscontrol.mobile.presentation.admin

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.*
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

class AdminInstallationsViewModel(
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val createInstallationUseCase: CreateInstallationUseCase,
    private val updateInstallationUseCase: UpdateInstallationUseCase,
    private val toggleInstallationStatusUseCase: ToggleInstallationStatusUseCase,
    private val getCheckpointsUseCase: GetCheckpointsUseCase,
    private val createCheckpointUseCase: CreateCheckpointUseCase,
    private val updateCheckpointUseCase: UpdateCheckpointUseCase,
    private val toggleCheckpointStatusUseCase: ToggleCheckpointStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(InstallationsState())
    val state: State<InstallationsState> = _state

    init {
        loadInstallations()
    }

    fun loadInstallations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getInstallationsUseCase()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        installations = list,
                        isLoading = false
                    )
                    list.forEach { inst ->
                        inst.id?.let { loadCheckpointCount(it) }
                    }
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar instalaciones"
                    )
                }
        }
    }

    private fun loadCheckpointCount(installationId: Long) {
        viewModelScope.launch {
            getCheckpointsUseCase(installationId)
                .onSuccess { list ->
                    val newCounts = _state.value.checkpointCounts.toMutableMap()
                    newCounts[installationId] = list.size
                    _state.value = _state.value.copy(checkpointCounts = newCounts)
                }
        }
    }

    fun loadCheckpointsForInstallation(installationId: Long) {
        viewModelScope.launch {
            Log.d("SIS_DEBUG", "Cargando checkpoints para instalación: $installationId")
            _state.value = _state.value.copy(isDetailLoading = true)
            getCheckpointsUseCase(installationId)
                .onSuccess { list ->
                    Log.d("SIS_DEBUG", "Checkpoints cargados: ${list.size}")
                    list.forEach { Log.d("SIS_DEBUG", "CP: ${it.name}, Status: ${it.status}") }
                    _state.value = _state.value.copy(
                        currentInstallationCheckpoints = list,
                        isDetailLoading = false
                    )
                }
                .onFailure { e ->
                    Log.e("SIS_DEBUG", "Error al cargar checkpoints", e)
                    _state.value = _state.value.copy(isDetailLoading = false, error = e.message)
                }
        }
    }

    fun createInstallation(
        name: String, 
        address: String, 
        clientName: String, 
        latitude: Double, 
        longitude: Double, 
        radius: Double
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, isCreateSuccess = false)
            val editorId = getEditorId()
            if (editorId <= 0L) {
                _state.value = _state.value.copy(isLoading = false, error = "Sesión no válida")
                return@launch
            }
            val request = InstallationRequestDto(
                name = name,
                address = address,
                clientName = clientName,
                latitude = latitude,
                longitude = longitude,
                radiusInMeters = radius
            )
            createInstallationUseCase(editorId, request)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isCreateSuccess = true)
                    loadInstallations()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun updateInstallation(installation: InstallationDto) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDetailLoading = true, error = null)
            val editorId = getEditorId()
            val id = installation.id ?: return@launch
            updateInstallationUseCase(id, editorId, installation)
                .onSuccess {
                    _state.value = _state.value.copy(isDetailLoading = false)
                    loadInstallations()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isDetailLoading = false, error = e.message)
                }
        }
    }

    fun toggleInstallationStatus(installationId: Long) {
        viewModelScope.launch {
            Log.d("SIS_DEBUG", "Toggling status para instalación: $installationId")
            _state.value = _state.value.copy(isDetailLoading = true)
            val editorId = getEditorId()
            toggleInstallationStatusUseCase(installationId, editorId)
                .onSuccess {
                    // Importante: Limpiamos isDetailLoading antes de recargar
                    _state.value = _state.value.copy(isDetailLoading = false)
                    loadInstallations()
                }
                .onFailure { e ->
                    Log.e("SIS_DEBUG", "Error toggle instalación", e)
                    _state.value = _state.value.copy(isDetailLoading = false, error = e.message)
                }
        }
    }

    fun createCheckpoint(installationId: Long, name: String, executionOrder: Int, nfcCode: String, desc: String, instruction: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDetailLoading = true)
            val editorId = getEditorId()
            val request = CheckpointRequestDto(
                name = name,
                executionOrder = executionOrder,
                nfcTagCode = nfcCode,
                locationDescription = desc,
                instruction = instruction,
                installation = InstallationIdRequest(id = installationId)
            )
            createCheckpointUseCase(editorId, request)
                .onSuccess {
                    loadCheckpointsForInstallation(installationId)
                    loadCheckpointCount(installationId)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isDetailLoading = false, error = e.message)
                }
        }
    }

    fun updateCheckpoint(checkpoint: CheckpointDto, installationId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDetailLoading = true)
            val editorId = getEditorId()
            
            // Aseguramos que incluya el objeto installation para cumplir con el contrato del backend
            val request = checkpoint.copy(
                installation = InstallationIdRequest(id = installationId)
            )
            
            updateCheckpointUseCase(checkpoint.id ?: 0L, editorId, request)
                .onSuccess {
                    loadCheckpointsForInstallation(installationId)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isDetailLoading = false, error = e.message)
                }
        }
    }

    fun toggleCheckpointStatus(checkpointId: Long, installationId: Long) {
        viewModelScope.launch {
            Log.d("SIS_DEBUG", "Toggling status para checkpoint: $checkpointId")
            _state.value = _state.value.copy(isDetailLoading = true)
            val editorId = getEditorId()
            
            if (editorId <= 0L) {
                Log.e("SIS_DEBUG", "EditorId inválido para toggle checkpoint")
                _state.value = _state.value.copy(isDetailLoading = false, error = "Usuario no identificado")
                return@launch
            }

            toggleCheckpointStatusUseCase(checkpointId, editorId)
                .onSuccess { newStatus ->
                    Log.d("SIS_DEBUG", "Toggle checkpoint éxito. Nuevo status: $newStatus")
                    loadCheckpointsForInstallation(installationId)
                }
                .onFailure { e ->
                    Log.e("SIS_DEBUG", "Error toggle checkpoint", e)
                    _state.value = _state.value.copy(isDetailLoading = false, error = e.message)
                }
        }
    }

    private suspend fun getEditorId(): Long {
        val sessionRoom = com.siscontrol.mobile.di.AppModule.getDatabase().userSessionDao().getSessionSync()
        val roomUserId = sessionRoom?.id ?: 0L
        val dsUserId = sessionManager.getUserIdSync() ?: 0L
        val finalId = if (roomUserId > 0) roomUserId else dsUserId
        Log.d("SIS_DEBUG", "EditorId detectado: $finalId (Room: $roomUserId, DS: $dsUserId)")
        return finalId
    }

    fun resetCreateState() {
        _state.value = _state.value.copy(isCreateSuccess = false, error = null)
    }
}

data class InstallationsState(
    val installations: List<InstallationDto> = emptyList(),
    val checkpointCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreateSuccess: Boolean = false,
    val isDetailLoading: Boolean = false,
    val currentInstallationCheckpoints: List<CheckpointDto> = emptyList()
)
