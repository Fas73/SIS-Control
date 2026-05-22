package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz Retrofit para la gestión de instalaciones y puntos de control.
 * Sincronizado exactamente con InstallationController.java del Backend.
 */
interface InstallationApiService {

    @GET("api/instalaciones")
    suspend fun getInstallations(): List<InstallationDto>

    /**
     * Crea una nueva instalación.
     */
    @POST("api/instalaciones")
    suspend fun createInstallation(
        @Query("editorId") editorId: Long,
        @Body request: InstallationRequestDto
    ): Response<Map<String, Any>>

    /**
     * Actualiza una instalación existente.
     */
    @PUT("api/instalaciones/{id}")
    suspend fun updateInstallation(
        @Path("id") id: Long,
        @Query("editorId") editorId: Long,
        @Body request: InstallationDto
    ): Response<Map<String, Any>>

    /**
     * Alterna el estado (Activo/Inactivo) de una instalación.
     */
    @PATCH("api/instalaciones/{id}/toggle-status")
    suspend fun toggleInstallationStatus(
        @Path("id") id: Long,
        @Query("editorId") editorId: Long
    ): Response<Map<String, Any>>

    /**
     * Obtiene los checkpoints de una instalación específica.
     * Revertido a la ruta estándar que sí funciona en tu Backend.
     */
    @GET("api/instalaciones/{id}/checkpoints")
    suspend fun getCheckpoints(
        @Path("id") installationId: Long
    ): List<CheckpointDto>

    /**
     * Crea un nuevo checkpoint (punto NFC).
     */
    @POST("api/instalaciones/checkpoints")
    suspend fun createCheckpoint(
        @Query("editorId") editorId: Long,
        @Body request: CheckpointRequestDto
    ): Response<Map<String, Any>>

    /**
     * Actualiza un checkpoint existente.
     */
    @PUT("api/instalaciones/checkpoints/{id}")
    suspend fun updateCheckpoint(
        @Path("id") id: Long,
        @Query("editorId") editorId: Long,
        @Body request: CheckpointDto
    ): Response<Map<String, Any>>

    /**
     * Alterna el estado (Activo/Inactivo) de un checkpoint.
     */
    @PATCH("api/instalaciones/checkpoints/{id}/toggle-status")
    suspend fun toggleCheckpointStatus(
        @Path("id") id: Long,
        @Query("editorId") editorId: Long
    ): Response<Map<String, Any>>
}
