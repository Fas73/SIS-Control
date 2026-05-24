package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.AdminDashboard
import com.siscontrol.mobile.domain.repository.ReportRepository

class GetAdminDashboardUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(): Result<AdminDashboard> {
        return repository.getAdminDashboard()
    }
}

