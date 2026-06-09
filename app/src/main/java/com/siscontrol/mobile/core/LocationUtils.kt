package com.siscontrol.mobile.core

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

object LocationUtils {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            // 1. Intentar obtener ubicación fresca de inmediato (con un token de cancelación)
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                com.google.android.gms.tasks.CancellationTokenSource().token
            ).await()
            
            if (location != null) {
                location
            } else {
                // 2. Si falla la fresca, usar la última que el teléfono recuerde (más rápido)
                fusedLocationClient.lastLocation.await()
            }
        } catch (e: Exception) {
            android.util.Log.e("LOCATION_UTILS", "Error obteniendo GPS: ${e.message}")
            null
        }
    }
}
