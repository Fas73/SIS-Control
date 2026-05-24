package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.AdminDashboard
import com.siscontrol.mobile.domain.model.CsvReportResponse
import com.siscontrol.mobile.domain.model.GuardRoundHistory
import java.io.InputStream

/**
 * Contrato de repositorio para reportes e historial de estadísticas.
 * Utiliza exclusivamente modelos de dominio.
 */
interface ReportRepository {
    suspend fun getAdminDashboard(): Result<AdminDashboard>
    suspend fun getGuardRoundsHistory(guardId: Long, inicio: String?, fin: String?): Result<GuardRoundHistory>
    suspend fun generateCsvReport(): Result<CsvReportResponse>
    // Se utilizará Intent ACTION_VIEW, pero dejamos el método de descarga por si se requiere después
    suspend fun downloadCsvReport(fileName: String): Result<InputStream>
}

