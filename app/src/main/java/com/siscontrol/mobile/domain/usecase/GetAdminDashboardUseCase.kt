package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.domain.repository.ReportRepository

class GetAdminDashboardUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(): Result<AdminDashboardResponse> {
        return repository.getAdminDashboard()
    }
}
