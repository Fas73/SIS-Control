package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.repository.IncidentRepository

class ReportIncidentUseCase(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(incident: IncidentDto): Result<IncidentDto> {
        if (incident.title.isBlank() || incident.description.isBlank()) {
            return Result.failure(Exception("El título y la descripción son obligatorios"))
        }
        return repository.saveIncident(incident)
    }
}
