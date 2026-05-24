package com.siscontrol.mobile.domain.model

data class Checkpoint(
    val id: Long,
    val name: String,
    val locationDescription: String?,
    val nfcTagCode: String,
    val executionOrder: Int,
    val instruction: String?,
    val status: Int,
    val installationId: Long
)

/**
 * Parámetros de dominio para la creación de un punto de control.
 */
data class CheckpointCreationParam(
    val name: String,
    val locationDescription: String,
    val nfcTagCode: String,
    val executionOrder: Int,
    val installationId: Long,
    val instruction: String? = null
)

