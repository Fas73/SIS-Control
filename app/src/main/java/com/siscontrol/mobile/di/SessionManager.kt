package com.siscontrol.mobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Gestor de Sesiones que utiliza Jetpack Preferences DataStore para persistencia segura.
 * 
 * Almacena el token JWT y el rol del usuario de forma asíncrona pero provee
 * acceso síncrono controlado para el interceptor de red.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sis_control_session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_ROLE = stringPreferencesKey("user_role")
    }

    /**
     * Guarda los datos de la sesión.
     */
    suspend fun saveSession(token: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            preferences[KEY_ROLE] = role
        }
    }

    /**
     * Recupera el token como un Flow para observar cambios.
     */
    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOKEN]
    }

    /**
     * Recupera el rol como un Flow.
     */
    val roleFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ROLE]
    }

    /**
     * Recupera el token de forma síncrona bloqueante (usado mayormente por el interceptor).
     */
    fun getTokenSync(): String? = runBlocking {
        context.dataStore.data.map { it[KEY_TOKEN] }.first()
    }

    /**
     * Limpia todos los datos de sesión (Logout).
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
