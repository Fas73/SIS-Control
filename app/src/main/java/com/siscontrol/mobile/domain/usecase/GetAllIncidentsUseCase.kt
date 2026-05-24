package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.Incident
import com.siscontrol.mobile.domain.repository.IncidentRepository

class GetAllIncidentsUseCase(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(): Result<List<Incident>> {
        return repository.getAllIncidents()
    }
}

