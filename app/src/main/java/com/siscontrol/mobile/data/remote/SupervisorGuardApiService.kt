package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.SupervisorGuardResponseDto
import retrofit2.Response
import retrofit2.http.*

interface SupervisorGuardApiService {

    @POST("api/supervisor-guard")
    suspend fun asignarGuardia(
        @Query("supervisorId") supervisorId: Long,
        @Query("guardId") guardId: Long
    ): Response<Map<String, Any>>

    @GET("api/supervisor-guard/{supervisorId}")
    suspend fun obtenerGuardiasDeSupervisor(
        @Path("supervisorId") supervisorId: Long
    ): List<SupervisorGuardResponseDto>

    @DELETE("api/supervisor-guard")
    suspend fun eliminarAsignacion(
        @Query("supervisorId") supervisorId: Long,
        @Query("guardId") guardId: Long
    ): Response<String>
}
