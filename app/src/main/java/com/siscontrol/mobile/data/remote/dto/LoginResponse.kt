package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Mapeo exacto de AuthResponseDTO del Backend.
 */
data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName(value = "id", alternate = ["userId"]) val id: Long?,
    @SerializedName("username") val username: String?,
    @SerializedName("fullName") val fullName: String?, // Ahora sí lo recibirá
    @SerializedName("role") val role: String?,
    @SerializedName("status") val status: String?
)