package com.siscontrol.mobile.presentation.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.repository.IncidentRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AdminHomeViewModel(
    private val getAdminDashboardUseCase: GetAdminDashboardUseCase,
    private val cancelRoundUseCase: CancelRoundUseCase,
    private val cancelShiftUseCase: CancelShiftUseCase,
    private val getShiftReportUseCase: GetShiftReportUseCase,
    private val incidentRepository: IncidentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(AdminHomeState())
    val state: State<AdminHomeState> = _state

    private val db = com.siscontrol.mobile.di.AppModule.getDatabase()
    private var isMonitoring = false

    init {
        loadDashboardData()
        startRealTimeMonitoring()
    }

    private fun startRealTimeMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        // Polling para conteos generales
        viewModelScope.launch {
            while (isMonitoring) {
                // Actualizar contadores y rondas cada 5 segundos (Polling reducido)
                updateAlertCountAndRounds()
                delay(10000)
            }
        }

        // WebSockets para alertas críticas instantáneas
        viewModelScope.launch {
            com.siscontrol.mobile.core.StompService.adminAlertFlow.collect {
                // Forzar actualización apenas llegue algo por socket
                updateAlertCountAndRounds()
            }
        }
    }

    private suspend fun updateAlertCountAndRounds() {
        // 1. Obtener datos generales del dashboard
        getAdminDashboardUseCase().onSuccess { data ->
            _state.value = _state.value.copy(
                totalGuards = data.totalGuards,
                activeShifts = data.activeShiftsCount,
                totalRoundsToday = data.totalRoundsToday,
                roundsInProgress = data.roundsInProgress,
                completedRoundsToday = data.roundsCompleted,
                totalInstallations = data.totalInstallations,
                activeInstallations = data.activeInstallationsCount,
                activeRounds = data.activeRoundsList.map { 
                    // Cálculo realista del progreso basado en puntos ejecutados vs totales
                    val calcProgress = if (it.checkpointsTotal > 0) 
                        it.checkpointsExecuted.toFloat() / it.checkpointsTotal 
                    else it.progreso

                    DashboardActiveRound(
                        id = it.id,
                        guardName = it.guardName,
                        location = it.location,
                        progress = calcProgress,
                        progressText = if (it.checkpointsTotal > 0) 
                            "Avance: ${it.checkpointsExecuted}/${it.checkpointsTotal} puntos" 
                            else it.statusDisplay,
                        status = it.status,
                        checkpointsExecuted = it.checkpointsExecuted,
                        checkpointsTotal = it.checkpointsTotal
                    )
                },
                activeShiftsList = data.activeShiftsList.map { 
                    DashboardActiveShift(it.id, it.guardName, it.location, it.entryTime)
                }
            )
        }

        // 2. Calcular el contador REAL de alertas (Considerando Swipes locales y fusiones)
        val dismissedIds = db.dismissedAlertDao().getAllDismissedIds().toSet()
        incidentRepository.getAllIncidents().onSuccess { list ->
            // Aplicamos la misma lógica de fusión que en la pantalla de Alertas
            val alertsWithImage = list.filter { it.imageUrl != null }
            val alertsWithoutImage = list.filter { it.imageUrl == null && it.title.contains("no escaneado", ignoreCase = true) }
            val redundantIds = mutableSetOf<Long>()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())

            alertsWithoutImage.forEach { sys ->
                val sysDate = try { sdf.parse(sys.createdAt ?: "") } catch(_: Exception) { null }
                val hasManualEquivalent = alertsWithImage.any { man ->
                    val manDate = try { sdf.parse(man.createdAt ?: "") } catch(_: Exception) { null }
                    val sameRound = man.roundExecutionId == sys.roundExecutionId
                    val samePoint = man.checkpointName == sys.checkpointName
                    val closeTime = if (manDate != null && sysDate != null) Math.abs(manDate.time - sysDate.time) < 120000 else true
                    sameRound && samePoint && closeTime
                }
                if (hasManualEquivalent) redundantIds.add(sys.id ?: 0L)
            }

            val finalActiveAlerts = list.filter { it.id !in dismissedIds && it.id !in redundantIds }
            
            _state.value = _state.value.copy(
                totalIncidents = finalActiveAlerts.size,
                pendingIncidents = finalActiveAlerts.count { it.severity.uppercase() == "ALTA" },
                highSeverityCount = finalActiveAlerts.count { it.severity.uppercase() == "ALTA" || it.title.contains("PÁNICO", ignoreCase = true) },
                mediumSeverityCount = finalActiveAlerts.count { it.severity.uppercase() == "MEDIA" },
                lowSeverityCount = finalActiveAlerts.count { it.severity.uppercase() == "BAJA" || it.title.contains("completada", ignoreCase = true) }
            )
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            updateAlertCountAndRounds()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun cancelRound(roundId: Long, motivo: String) {
        viewModelScope.launch {
            val adminId = sessionManager.getUserIdSync() ?: 0L
            _state.value = _state.value.copy(isLoading = true)
            cancelRoundUseCase(roundId, adminId, motivo)
                .onSuccess { 
                    // El Backend ya guardó la etiqueta en las observaciones
                    _state.value = _state.value.copy(successMessage = "Acción realizada con éxito")
                    loadDashboardData() 
                }
                .onFailure { e -> 
                    _state.value = _state.value.copy(isLoading = false, error = com.siscontrol.mobile.core.ErrorUtils.parse(e)) 
                }
        }
    }

    fun cancelShift(shiftId: Long, motivo: String) {
        viewModelScope.launch {
            val adminId = sessionManager.getUserIdSync() ?: 0L
            _state.value = _state.value.copy(isLoading = true)
            cancelShiftUseCase(shiftId, adminId, motivo)
                .onSuccess { 
                    _state.value = _state.value.copy(successMessage = "Jornada cortada administrativamente")
                    loadDashboardData() 
                }
                .onFailure { e -> 
                    _state.value = _state.value.copy(isLoading = false, error = com.siscontrol.mobile.core.ErrorUtils.parse(e)) 
                }
        }
    }

    /**
     * Obtiene el reporte consolidado de una jornada para su exportación a PDF.
     */
    fun getShiftReport(shiftId: Long, onResult: (com.siscontrol.mobile.data.remote.dto.ShiftReportDto?) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getShiftReportUseCase(shiftId)
                .onSuccess { report ->
                    _state.value = _state.value.copy(isLoading = false)
                    onResult(report)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false, 
                        error = "Error al obtener reporte: ${e.message}"
                    )
                    onResult(null)
                }
        }
    }
    
    fun resetMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        isMonitoring = false
    }
}

data class AdminHomeState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val totalGuards: Int = 0,
    val activeShifts: Int = 0,
    val totalRoundsToday: Int = 0,
    val roundsInProgress: Int = 0,
    val completedRoundsToday: Int = 0,
    val totalIncidents: Int = 0,
    val pendingIncidents: Int = 0,
    val highSeverityCount: Int = 0,
    val mediumSeverityCount: Int = 0,
    val lowSeverityCount: Int = 0,
    val totalInstallations: Int = 0,
    val activeInstallations: Int = 0,
    val activeRounds: List<DashboardActiveRound> = emptyList(),
    val activeShiftsList: List<DashboardActiveShift> = emptyList(),
    val error: String? = null
)

data class DashboardActiveRound(
    val id: Long,
    val guardName: String,
    val location: String,
    val progress: Float,
    val progressText: String,
    val status: String,
    val checkpointsExecuted: Int = 0,
    val checkpointsTotal: Int = 0
)

data class DashboardActiveShift(
    val id: Long,
    val guardName: String,
    val location: String,
    val entryTime: String
)
