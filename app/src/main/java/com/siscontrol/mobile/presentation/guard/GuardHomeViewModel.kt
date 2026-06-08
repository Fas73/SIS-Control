package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.RoundApiService
import com.siscontrol.mobile.data.remote.dto.CurrentStateResponseDto
import com.siscontrol.mobile.di.SessionManager
import kotlinx.coroutines.launch

sealed class GuardNavigationState {
    data class Idle(val estado: com.siscontrol.mobile.data.remote.dto.CurrentStateResponseDto? = null) : GuardNavigationState()
    object Loading : GuardNavigationState()
    data class Redirect(val route: String) : GuardNavigationState()
    data class Error(val message: String) : GuardNavigationState()
}

class GuardHomeViewModel(
    private val getCurrentGuardStateUseCase: com.siscontrol.mobile.domain.usecase.GetCurrentGuardStateUseCase,
    private val triggerPanicUseCase: com.siscontrol.mobile.domain.usecase.TriggerPanicUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _navState = mutableStateOf<GuardNavigationState>(GuardNavigationState.Idle())
    val navState: State<GuardNavigationState> = _navState

    // NUEVO: Estado para alertas administrativas en tiempo real
    private val _adminNotification = mutableStateOf<String?>(null)
    val adminNotification: State<String?> = _adminNotification

    init {
        startRealTimeListener()
    }

    private fun startRealTimeListener() {
        viewModelScope.launch {
            com.siscontrol.mobile.core.StompService.adminAlertFlow.collect { incident ->
                val userId = sessionManager.getUserIdSync() ?: 0L
                val myFullName = sessionManager.getFullNameSync() ?: ""
                
                // CRITERIO MEJORADO: Verificar por ID de trabajador o por Título/Descripción
                val isMyIncident = incident.roundExecution?.workerId == userId || 
                                 incident.username == myFullName ||
                                 incident.description.contains(myFullName)

                if (isMyIncident) {
                    val isShiftCancellation = incident.title == "JORNADA FINALIZADA" || 
                                            incident.description.contains("[CANCELACIÓN ADMINISTRATIVA]") || 
                                            incident.description.contains("[CIERRE AUTOMÁTICO]")

                    if (isShiftCancellation) {
                        android.util.Log.d("GUARD_HOME_VM", "¡Cierre de jornada detectado vía WebSocket!")
                        
                        // Limpiar y embellecer el mensaje
                        val cleanMessage = when {
                            incident.description.contains("[CANCELACIÓN") -> 
                                "Estimado colaborador, su jornada laboral ha sido finalizada administrativamente por la jefatura. Por favor, acérquese a su supervisor."
                            incident.description.contains("[CIERRE AUTOMÁTICO]") -> 
                                "Su jornada ha finalizado automáticamente según el horario programado. Gracias por su compromiso."
                            else -> "Su jornada laboral ha sido finalizada exitosamente."
                        }
                        
                        _adminNotification.value = cleanMessage
                        
                        // Forzar refresco inmediato del estado para volver a la pantalla azul
                        checkStatusAndRedirect("", "", silent = true)
                    }
                }
            }
        }
        
        // MOTOR DE SEGURIDAD (Polling cada 30s): Fallback si el WebSocket falla
        viewModelScope.launch {
            while (true) {
                val currentState = _navState.value
                if (currentState is GuardNavigationState.Idle && currentState.estado?.jornadaActiva == true) {
                    android.util.Log.d("GUARD_HOME_VM", "Polling de seguridad: Verificando vigencia de jornada...")
                    checkStatusAndRedirect("", "", silent = true)
                }
                kotlinx.coroutines.delay(30000)
            }
        }
    }

    fun clearNotification() {
        _adminNotification.value = null
    }

    fun clearRedirect() {
        if (_navState.value is GuardNavigationState.Redirect) {
            _navState.value = GuardNavigationState.Idle()
        }
    }

    private var isLocalAction = false

    fun notifyLocalAction() {
        isLocalAction = true
    }

    fun checkStatusAndRedirect(token: String, role: String, silent: Boolean = false, isLocal: Boolean = false) {
        if (isLocal) isLocalAction = true
        
        viewModelScope.launch {
            val wasShiftActive = (_navState.value as? GuardNavigationState.Idle)?.estado?.jornadaActiva == true
            
            if (!silent) {
                _navState.value = GuardNavigationState.Loading
            }
            try {
                val userId = sessionManager.getUserId() ?: 0L
                if (userId == 0L) {
                    _navState.value = GuardNavigationState.Idle()
                    return@launch
                }

                getCurrentGuardStateUseCase(userId)
                    .onSuccess { data ->
                        // LÓGICA CRÍTICA: Si el guardia cree que está en turno (verde) pero el server dice que NO (azul)
                        // SOLO activar el aviso si NO fue una acción local (presionar el botón de finalizar)
                        if (wasShiftActive && data.jornadaActiva == false && !isLocalAction) {
                            android.util.Log.w("GUARD_HOME_VM", "¡DISCREPANCIA DETECTADA! La jefatura cerró la jornada.")
                            _adminNotification.value = "Estimado colaborador, su jornada laboral ha sido finalizada administrativamente por la jefatura. Por favor, acérquese a su supervisor para más información."
                            sessionManager.clearActiveSession()
                        }
                        
                        if (data.jornadaActiva == false) {
                            isLocalAction = false // Resetear flag
                        }

                        // Sincronizar datos locales con el servidor
                        viewModelScope.launch {
                            if (data.jornadaActiva == true && data.jornada?.installation != null) {
                                val inst = data.jornada.installation
                                sessionManager.saveActiveInstallation(inst.id ?: 0L, inst.clientName ?: inst.name ?: "Sede")
                                if (data.rondaActiva == true && data.ronda != null) {
                                    sessionManager.saveActiveRound(data.ronda.id ?: 0L)
                                }
                            }
                        }

                        // ESCENARIO D: Pendiente de Selfie (Enrolamiento)
                        // COMENTADO: Se permite navegar al Home aunque no tenga foto para no bloquear la operación.
                        /*
                        if (data.user?.imageUrl.isNullOrBlank()) {
                            android.util.Log.d("GUARD_HOME_VM", "Redirigiendo a Perfil por falta de foto")
                            val route = com.siscontrol.mobile.presentation.Destinos.guardProfileRoute(token, role)
                            _navState.value = GuardNavigationState.Redirect(route)
                        }
                        */
                        
                        // ESCENARIO C: Ronda Activa -> Redirección inmediata
                        if (data.rondaActiva == true && data.ronda != null) {
                            android.util.Log.d("GUARD_HOME_VM", "Redirigiendo a Ronda Activa: ${data.ronda.id}")
                            val inst = data.jornada?.installation
                            val route = com.siscontrol.mobile.presentation.Destinos.guardRondaRoute(
                                token = token,
                                role = role,
                                roundId = data.ronda.id ?: 0L,
                                installationId = inst?.id ?: 0L,
                                installationName = inst?.clientName ?: inst?.name ?: "Instalación"
                            )
                            _navState.value = GuardNavigationState.Redirect(route)
                        } 
                        // ESCENARIO A y B: Se queda en Home pero con datos actualizados
                        else {
                            android.util.Log.d("GUARD_HOME_VM", "Estado Idle - Permaneciendo en Home")
                            _navState.value = GuardNavigationState.Idle(data)
                        }
                    }
                    .onFailure { e ->
                        android.util.Log.e("GUARD_HOME_VM", "Falla al obtener estado: ${e.message}")
                        // Si falla (ej. 404), nos quedamos en Home vacío por ahora para no romper el flujo
                        _navState.value = GuardNavigationState.Idle()
                    }
            } catch (e: Exception) {
                _navState.value = GuardNavigationState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun triggerPanicAlert() {
        viewModelScope.launch {
            val currentState = (_navState.value as? GuardNavigationState.Idle)?.estado
            val shiftId = currentState?.jornada?.id
            if (shiftId != null) {
                android.util.Log.d("GUARD_HOME_VM", "Triggering panic for Shift ID: $shiftId")
                triggerPanicUseCase(shiftId = shiftId, descripcion = "Solicitud de ayuda inmediata desde el botón de pánico.")
            } else {
                android.util.Log.e("GUARD_HOME_VM", "Cannot trigger panic: No active shift ID found.")
            }
        }
    }
}
