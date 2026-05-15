package com.siscontrol.mobile.presentation.guard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.domain.usecase.EndRoundUseCase
import kotlinx.coroutines.launch

class GuardRoundViewModel(
    private val endRoundUseCase: EndRoundUseCase
) : ViewModel() {

    private val _state = mutableStateOf(GuardRoundState())
    val state: State<GuardRoundState> = _state

    fun endRound(roundId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            endRoundUseCase(roundId)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al finalizar ronda"
                    )
                }
        }
    }
}

data class GuardRoundState(
    val isLoading: Boolean = false,
    val error: String? = null
)
