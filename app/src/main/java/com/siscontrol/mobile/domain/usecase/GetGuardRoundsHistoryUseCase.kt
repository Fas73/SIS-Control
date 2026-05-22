package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse
import com.siscontrol.mobile.domain.repository.ReportRepository

class GetGuardRoundsHistoryUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(guardId: Long, inicio: String? = null, fin: String? = null): Result<GuardRoundHistoryResponse> {
        return repository.getGuardRoundsHistory(guardId, inicio, fin)
    }
}
