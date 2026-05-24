package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.AdminDashboard
import com.siscontrol.mobile.domain.model.GuardRoundHistory

/**
 * Contrato de repositorio para reportes e historial de estadísticas.
 * Utiliza exclusivamente modelos de dominio.
 */
interface ReportRepository {
    suspend fun getAdminDashboard(): Result<AdminDashboard>
    suspend fun getGuardRoundsHistory(guardId: Long, inicio: String?, fin: String?): Result<GuardRoundHistory>
}

