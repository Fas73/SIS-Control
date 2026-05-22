package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse

interface ReportRepository {
    suspend fun getAdminDashboard(): Result<AdminDashboardResponse>
    suspend fun getGuardRoundsHistory(guardId: Long, inicio: String?, fin: String?): Result<GuardRoundHistoryResponse>
}
