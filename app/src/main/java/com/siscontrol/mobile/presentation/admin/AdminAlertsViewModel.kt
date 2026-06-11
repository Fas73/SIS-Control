package com.siscontrol.mobile.presentation.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.repository.IncidentRepository
import com.siscontrol.mobile.data.local.entities.DismissedAlertEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminAlertsViewModel(
    private val incidentRepository: IncidentRepository,
    private val sessionManager: com.siscontrol.mobile.di.SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(AdminAlertsState())
    val state: State<AdminAlertsState> = _state

    private val db = com.siscontrol.mobile.di.AppModule.getDatabase()
    private var isMonitoring = false

    init {
        loadInitialAlerts()
        startRealTimeMonitoring()
    }

    private fun startRealTimeMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        viewModelScope.launch {
            while (isMonitoring) {
                loadAlertsSilently()
                delay(4000)
            }
        }
    }

    private fun loadInitialAlerts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val dismissedIds = db.dismissedAlertDao().getAllDismissedIds().toSet()
            
            val userRole = sessionManager.getUserRoleSync() ?: "ADMIN"
            val userId = sessionManager.getUserIdSync() ?: 0L
            val supervisorId = if (userRole == "SUPERVISOR" && userId > 0L) userId else null

            incidentRepository.getAllIncidents(supervisorId)
                .onSuccess { list ->
                    // FUSIÓN DE ALERTAS REDUNDANTES (Misma Ronda + Mismo Punto + Misma Hora)
                    val alertsWithImage = list.filter { it.imageUrl != null }
                    val alertsWithoutImage = list.filter { it.imageUrl == null && it.title.contains("no escaneado", ignoreCase = true) }
                    
                    val redundantIds = mutableSetOf<Long>()
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())

                    alertsWithoutImage.forEach { sys ->
                        val sysDate = try { sdf.parse(sys.createdAt ?: "") } catch(e: Exception) { null }
                        
                        val hasManualEquivalent = alertsWithImage.any { man ->
                            val manDate = try { sdf.parse(man.createdAt ?: "") } catch(e: Exception) { null }
                            
                            // Criterios de Fusión:
                            val sameRound = man.roundExecutionId == sys.roundExecutionId
                            val samePoint = man.checkpointName == sys.checkpointName
                            
                            // Diferencia de tiempo menor a 2 minutos (120,000 ms)
                            val closeTime = if (manDate != null && sysDate != null) {
                                Math.abs(manDate.time - sysDate.time) < 120000
                            } else true

                            sameRound && samePoint && closeTime
                        }
                        if (hasManualEquivalent) redundantIds.add(sys.id ?: 0L)
                    }

                    val filtered = list.filter { it.id !in dismissedIds && it.id !in redundantIds }
                    _state.value = _state.value.copy(
                        alerts = filtered.sortedByDescending { it.createdAt },
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    private fun loadAlertsSilently() {
        viewModelScope.launch {
            val dismissedIds = db.dismissedAlertDao().getAllDismissedIds().toSet()
            
            val userRole = sessionManager.getUserRoleSync() ?: "ADMIN"
            val userId = sessionManager.getUserIdSync() ?: 0L
            val supervisorId = if (userRole == "SUPERVISOR" && userId > 0L) userId else null

            incidentRepository.getAllIncidents(supervisorId)
                .onSuccess { list ->
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
                            val closeTime = if (manDate != null && sysDate != null) {
                                Math.abs(manDate.time - sysDate.time) < 120000
                            } else true

                            sameRound && samePoint && closeTime
                        }
                        if (hasManualEquivalent) redundantIds.add(sys.id ?: 0L)
                    }

                    val filteredList = list.filter { it.id !in dismissedIds && it.id !in redundantIds }
                        .sortedByDescending { it.createdAt }
                    
                    if (filteredList != _state.value.alerts) {
                        _state.value = _state.value.copy(alerts = filteredList)
                    }
                }
        }
    }

    fun dismissAlert(alertId: Long?) {
        if (alertId == null) return
        viewModelScope.launch {
            // 1. Guardar en la base de datos interna (permanente)
            db.dismissedAlertDao().markAsDismissed(DismissedAlertEntity(alertId))
            
            // 2. Quitar de la vista actual
            _state.value = _state.value.copy(
                alerts = _state.value.alerts.filter { it.id != alertId }
            )
        }
    }

    fun manageAlert(alertId: Long?) {
        if (alertId == null) return
        viewModelScope.launch {
            // Por ahora marcamos como gestionada y removemos de la vista 
            // (podría integrarse con backend en el futuro)
            db.dismissedAlertDao().markAsDismissed(DismissedAlertEntity(alertId))

            _state.value = _state.value.copy(
                alerts = _state.value.alerts.filter { it.id != alertId }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        isMonitoring = false
    }

    /**
     * Obtiene el reporte de jornada vinculado a una alerta de término de turno.
     */
    fun getShiftReportForAlert(shiftId: Long, onResult: (com.siscontrol.mobile.data.remote.dto.ShiftReportDto?) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            com.siscontrol.mobile.di.AppModule.getShiftReportUseCase(shiftId)
                .onSuccess { report ->
                    _state.value = _state.value.copy(isLoading = false)
                    onResult(report)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false)
                    onResult(null)
                }
        }
    }
}

data class AdminAlertsState(
    val alerts: List<IncidentDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
