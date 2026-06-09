package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.GetGuardRoundsHistoryUseCase
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

class GuardHistoryViewModel(
    private val getGuardRoundsHistoryUseCase: GetGuardRoundsHistoryUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = mutableStateOf(GuardHistoryState())
    val state: State<GuardHistoryState> = _state

    fun loadHistory(filter: String) {
        viewModelScope.launch {
            val guardId = sessionManager.getUserIdSync() ?: return@launch
            _state.value = _state.value.copy(isLoading = true, error = null)

            val (inicio, fin) = calculateDateRange(filter)

            getGuardRoundsHistoryUseCase(guardId, inicio, fin)
                .onSuccess { response ->
                    // Respetamos la trazabilidad absoluta del Backend consolidado por ID.
                    val cleanRondas = response.rondas.distinctBy { it.id }

                    // Recalculamos contadores locales para que la UI coincida con los datos del servidor
                    val consolidatedHistory = response.copy(
                        rondas = cleanRondas,
                        total = cleanRondas.size,
                        completas = cleanRondas.count { it.statusDisplay?.equals("Completada", ignoreCase = true) == true }
                    )
                    
                    _state.value = _state.value.copy(
                        history = consolidatedHistory,
                        isLoading = false
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

    private fun calculateDateRange(filter: String): Pair<String?, String?> {
        val now = LocalDateTime.now()
        // Formato estricto yyyy-MM-dd'T'HH:mm:ss solicitado por el backend
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        return when (filter) {
            "Hoy" -> {
                val inicio = now.withHour(0).withMinute(0).withSecond(0).format(formatter)
                val fin = now.withHour(23).withMinute(59).withSecond(59).format(formatter)
                Pair(inicio, fin)
            }
            "Esta Semana" -> {
                val inicio = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .withHour(0).withMinute(0).withSecond(0).format(formatter)
                val fin = now.withHour(23).withMinute(59).withSecond(59).format(formatter)
                Pair(inicio, fin)
            }
            "Este Mes" -> {
                val inicio = now.with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0).withMinute(0).withSecond(0).format(formatter)
                val fin = now.withHour(23).withMinute(59).withSecond(59).format(formatter)
                Pair(inicio, fin)
            }
            else -> Pair(null, null)
        }
    }
}

data class GuardHistoryState(
    val history: GuardRoundHistoryResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
