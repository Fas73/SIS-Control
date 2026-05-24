package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.model.GuardRoundHistory
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
                    _state.value = _state.value.copy(
                        history = response,
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
        val formatter = DateTimeFormatter.ISO_DATE_TIME

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
    val history: GuardRoundHistory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
