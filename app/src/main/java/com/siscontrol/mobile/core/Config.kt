package com.siscontrol.mobile.core

object Config {
    private const val API_PATH = "api/"  // <-- AGREGADO

    const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/"
    const val BASE_URL_DEVICE   = "http://192.168.1.103:8080/" // IP local real
    const val USE_DEVICE = false  // CAMBIAR a TRUE usando un TELEFONO
    val BASE_URL: String = if (USE_DEVICE) BASE_URL_DEVICE else BASE_URL_EMULATOR

    const val AUTH_PREFERENCE = "AUTH_PREF"
    const val AUTH_KEY = "AUTH_KEY" // token
    const val USER_ID_KEY = "USER_ID"
}
