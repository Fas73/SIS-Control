package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.domain.repository.ReportRepository

class GetSupervisorDashboardUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(supervisorId: Long): Result<AdminDashboardResponse> {
        return repository.getSupervisorDashboard(supervisorId)
    }
}
