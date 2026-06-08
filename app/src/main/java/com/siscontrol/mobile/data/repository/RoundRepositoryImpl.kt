package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.RoundApiService
import com.siscontrol.mobile.data.remote.dto.*
import com.siscontrol.mobile.domain.repository.RoundRepository

class RoundRepositoryImpl(
    private val api: RoundApiService,
    private val sessionManager: com.siscontrol.mobile.di.SessionManager
) : RoundRepository {

    override suspend fun getCurrentState(userId: Long): Result<com.siscontrol.mobile.data.remote.dto.CurrentStateResponseDto> {
        return try {
            val response = api.getCurrentState(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener estado actual: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllRounds(): Result<List<RoundResponseDto>> {
        return try {
            val response = api.getAllRounds()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startRound(userId: Long, installationId: Long, latitude: Double?, longitude: Double?): Result<Long> {
        return try {
            val response = api.startRound(userId, installationId, latitude, longitude)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val roundId = body.ronda?.id ?: 0L
                Result.success(roundId)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                val cleanMessage = if (errorMsg.contains("\"mensaje\":\"")) {
                    errorMsg.substringAfter("\"mensaje\":\"").substringBefore("\"")
                } else "Error ${response.code()}: Contacte al Admin"
                
                Result.failure(Exception(cleanMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun endRound(roundId: Long, observations: String): Result<Unit> {
        return try {
            val response = api.endRound(roundId, EndRoundRequest(observations))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                // Si el error es 400 y el mensaje dice que ya está finalizada, lo tratamos como éxito
                if (response.code() == 400 && errorMsg.contains("finalizada anteriormente", ignoreCase = true)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error ${response.code()}: $errorMsg"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoundDetail(roundId: Long): Result<com.siscontrol.mobile.data.remote.dto.RoundDetailResponseDto> {
        return try {
            val currentUserId = sessionManager.getUserIdSync() ?: 0L
            val response = api.getRoundDetail(roundId, currentUserId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanCheckpoint(roundId: Long, checkpointId: Long, comment: String, status: Int, imageUrl: String?, scannedAt: String?, latitude: Double?, longitude: Double?): Result<Long?> {
        return try {
            // 1. Formateo estricto de fecha yyyy-MM-dd'T'HH:mm:ss (El backend requiere segundos obligatorios)
            val cleanScannedAt = try {
                if (scannedAt != null) {
                    val inputDate = scannedAt.substringBefore(".") // Quitar milisegundos si existen
                    if (inputDate.count { it == ':' } == 1) {
                        "$inputDate:00" // Añadir segundos si faltan (formato HH:mm -> HH:mm:ss)
                    } else {
                        inputDate
                    }
                } else {
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                }
            } catch (e: Exception) {
                scannedAt?.replace(" ", "T")
            }

            // 2. CONSTRUCCIÓN DEL DTO PLANO (Consistente con IncidentDto y pedido de Backend)
            val request = ScanCheckpointRequest(
                roundExecutionId = roundId,
                checkpointId = checkpointId,
                status = status,
                notes = comment,
                imageUrl = if (!imageUrl.isNullOrBlank()) {
                    if (imageUrl.contains("firebasestorage")) {
                        imageUrl.split("?").first() + "?alt=media"
                    } else imageUrl
                } else null,
                latitude = latitude,
                longitude = longitude,
                scannedAt = cleanScannedAt
            )

            // 3. LOGGING PARA DEPURACIÓN
            val logGson = com.google.gson.GsonBuilder().disableHtmlEscaping().create()
            val jsonBody = logGson.toJson(request)
            android.util.Log.e("CRITICAL_FRONTEND", "🚀 ENVIANDO JSON (FORMATO PLANO - CONSISTENTE): $jsonBody")

            val response = api.scanCheckpoint(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val escaneoMap = body["escaneo"] as? Map<*, *>
                val id = (escaneoMap?.get("id") as? Number)?.toLong()
                android.util.Log.d("CRITICAL_FRONTEND", "✅ ÉXITO EN MySQL")
                Result.success(id)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error ${response.code()}"
                android.util.Log.e("CRITICAL_FRONTEND", "❌ RECHAZO: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            android.util.Log.e("CRITICAL_FRONTEND", "❌ FALLA CRÍTICA: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun cancelRound(roundId: Long, adminId: Long, motivo: String?): Result<Unit> {
        return try {
            val response = api.cancelRoundAdministratively(roundId, adminId, motivo)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cancelar ronda: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelShift(shiftId: Long, adminId: Long, motivo: String?): Result<Unit> {
        return try {
            val response = api.cancelShiftAdministratively(shiftId, adminId, motivo)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cancelar jornada: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
