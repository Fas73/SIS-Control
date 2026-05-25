package com.siscontrol.mobile.core

object Config {
    // URL Base para el emulador
    const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/"

    // IP de tu PC/Mac (Cámbiala por la tuya real si usas celular)
    //const val BASE_URL_DEVICE   = "http://192.168.100.4:8080/"

    // CAMBIO AQUÍ: Al usar 'adb reverse', el celular debe apuntar a localhost por USB
    //const val BASE_URL_DEVICE   = "http://localhost:8080/"
    const val BASE_URL_DEVICE   = "https://budget-poison-felt-tip.ngrok-free.dev/"
    // true = celular físico (vía USB con adb reverse)
    // false = emulador Android Studio
    const val USE_DEVICE = true

    // La URL final calculada dinámicamente
    val BASE_URL: String = if (USE_DEVICE) BASE_URL_DEVICE else BASE_URL_EMULATOR

    const val AUTH_PREFERENCE = "AUTH_PREF"
    const val AUTH_KEY = "AUTH_KEY"
    const val USER_ID_KEY = "USER_ID"
}