package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.mapper.toDomain
import com.siscontrol.mobile.data.mapper.toDto
import com.siscontrol.mobile.domain.model.AdminDashboard
import com.siscontrol.mobile.domain.model.GuardRoundHistory
import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse
import com.siscontrol.mobile.domain.repository.ReportRepository
import com.siscontrol.mobile.data.remote.ReportApiService
class ReportRepositoryImpl(
    private val api: ReportApiService
) : ReportRepository {

    override suspend fun getAdminDashboard(): Result<AdminDashboard> {
        return try {
            Result.success(api.getAdminDashboard().toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGuardRoundsHistory(guardId: Long, inicio: String?, fin: String?): Result<GuardRoundHistory> {
        return try {
            Result.success(api.getGuardRoundsHistory(guardId, inicio, fin).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
