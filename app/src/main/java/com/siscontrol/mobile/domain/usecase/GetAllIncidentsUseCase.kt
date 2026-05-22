package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.data.remote.dto.IncidentDto
import com.siscontrol.mobile.domain.repository.IncidentRepository

class GetAllIncidentsUseCase(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(): Result<List<IncidentDto>> {
        return repository.getAllIncidents()
    }
}
