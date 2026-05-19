package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.ReportApiService
import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse
import com.siscontrol.mobile.domain.repository.ReportRepository

class ReportRepositoryImpl(
    private val api: ReportApiService
) : ReportRepository {

    override suspend fun getAdminDashboard(): Result<AdminDashboardResponse> {
        return try {
            Result.success(api.getAdminDashboard())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGuardRoundsHistory(guardId: Long, inicio: String?, fin: String?): Result<GuardRoundHistoryResponse> {
        return try {
            Result.success(api.getGuardRoundsHistory(guardId, inicio, fin))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
