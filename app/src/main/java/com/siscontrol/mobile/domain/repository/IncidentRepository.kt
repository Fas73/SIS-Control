package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.IncidentDto

interface IncidentRepository {
    suspend fun saveIncident(incident: IncidentDto): Result<IncidentDto>
    suspend fun getAllIncidents(): Result<List<IncidentDto>>
}