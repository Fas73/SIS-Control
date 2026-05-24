package com.siscontrol.mobile.domain.model

data class CsvReportResponse(
    val fileName: String,
    val downloadUrl: String,
    val rows: Int
)
