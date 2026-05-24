package com.siscontrol.mobile.data.remote.dto

data class CsvReportResponseDto(
    val fileName: String,
    val filePath: String,
    val downloadUrl: String,
    val generatedAt: String,
    val rows: Int
)
