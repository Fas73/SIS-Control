package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.AdminDashboardResponse
import com.siscontrol.mobile.data.remote.dto.GuardRoundHistoryResponse
import com.siscontrol.mobile.data.remote.dto.GuardStatsResponse
import com.siscontrol.mobile.data.remote.dto.RoundStatsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportApiService {

    @GET("api/reportes/dashboard-admin")
    suspend fun getAdminDashboard(): AdminDashboardResponse

    @GET("api/reportes/mis-rondas/{guardId}")
    suspend fun getGuardRoundsHistory(
        @Path("guardId") guardId: Long,
        @Query("inicio") inicio: String? = null,
        @Query("fin") fin: String? = null
    ): GuardRoundHistoryResponse
}
