package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.GuardRoundHistory
import com.siscontrol.mobile.domain.repository.ReportRepository

class GetGuardRoundsHistoryUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(guardId: Long, inicio: String? = null, fin: String? = null): Result<GuardRoundHistory> {
        return repository.getGuardRoundsHistory(guardId, inicio, fin)
    }
}

