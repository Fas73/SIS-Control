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
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _navState = mutableStateOf<GuardNavigationState>(GuardNavigationState.Idle())
    val navState: State<GuardNavigationState> = _navState

    fun clearRedirect() {
        if (_navState.value is GuardNavigationState.Redirect) {
            _navState.value = GuardNavigationState.Idle()
        }
    }

    fun checkStatusAndRedirect(token: String, role: String, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _navState.value = GuardNavigationState.Loading
            }
            try {
                val userId = sessionManager.getUserId() ?: 0L
                android.util.Log.d("GUARD_HOME_VM", "Consultando estado para usuario ID: $userId")
                
                if (userId == 0L) {
                    _navState.value = GuardNavigationState.Idle()
                    return@launch
                }

                getCurrentGuardStateUseCase(userId)
                    .onSuccess { data ->
                        android.util.Log.d("GUARD_HOME_VM", "Estado recibido: $data")
                        
                        // Sincronizar datos locales con el servidor por si hubo logout/falla
                        viewModelScope.launch {
                            if (data.jornadaActiva == true && data.jornada?.installation != null) {
                                val inst = data.jornada.installation
                                sessionManager.saveActiveInstallation(inst.id ?: 0L, inst.clientName ?: inst.name ?: "Sede")
                                if (data.rondaActiva == true && data.ronda != null) {
                                    sessionManager.saveActiveRound(data.ronda.id ?: 0L)
                                }
                            }
                        }

                        // ESCENARIO C: Ronda Activa -> Redirección inmediata
                        if (data.rondaActiva == true && data.ronda != null) {
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
}
