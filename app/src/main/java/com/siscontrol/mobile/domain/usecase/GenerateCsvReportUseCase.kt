package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.model.CsvReportResponse
import com.siscontrol.mobile.domain.repository.ReportRepository

class GenerateCsvReportUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(): Result<CsvReportResponse> {
        return repository.generateCsvReport()
    }
}
