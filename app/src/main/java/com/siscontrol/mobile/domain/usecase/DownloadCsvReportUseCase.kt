package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.ReportRepository
import java.io.InputStream

class DownloadCsvReportUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(fileName: String): Result<InputStream> {
        return repository.downloadCsvReport(fileName)
    }
}
