package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.domain.model.Incident

/**
 * Contrato de repositorio para el reporte de incidentes y alertas de pánico.
 * Trabaja exclusivamente con el modelo de dominio Incident.
 */
interface IncidentRepository {
    suspend fun saveIncident(incident: Incident): Result<Incident>
    suspend fun getAllIncidents(): Result<List<Incident>>
    suspend fun triggerPanic(roundId: Long, descripcion: String? = null): Result<Unit>
    suspend fun analizarIA(id: Long): Result<Incident>
}

