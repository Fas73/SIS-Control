package com.siscontrol.mobile.core

object Config {
    // URL Base para el emulador (Mac -> Emulador)
    const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/"

    // IP de tu Mac (Cámbiala por la tuya real si usas celular)
    const val BASE_URL_DEVICE   = "http://192.168.1.103:8080/"

    const val USE_DEVICE = false

    // La URL final que usará Retrofit
    val BASE_URL: String = if (USE_DEVICE) BASE_URL_DEVICE else BASE_URL_EMULATOR

    // Keys para guardar la sesión localmente
    const val AUTH_PREFERENCE = "AUTH_PREF"
    const val AUTH_KEY = "AUTH_KEY" // Aquí guardarás el mensaje o token
    const val USER_ID_KEY = "USER_ID"
}