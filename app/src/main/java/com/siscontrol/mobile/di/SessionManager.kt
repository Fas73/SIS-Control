package com.siscontrol.mobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Gestor de Sesiones que utiliza Jetpack Preferences DataStore para persistencia segura.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sis_control_session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_ROLE = stringPreferencesKey("user_role")
        private val KEY_FULL_NAME = stringPreferencesKey("user_full_name")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_ACTIVE_INSTALLATION_ID = longPreferencesKey("active_installation_id")
        private val KEY_ACTIVE_ROUND_ID = longPreferencesKey("active_round_id")
        private val KEY_ACTIVE_INSTALLATION_NAME = stringPreferencesKey("active_installation_name")
    }

    /**
     * Guarda la información de la ronda y jornada activa.
     */
    suspend fun saveActiveSession(installationId: Long, roundId: Long, installationName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_INSTALLATION_ID] = installationId
            preferences[KEY_ACTIVE_ROUND_ID] = roundId
            preferences[KEY_ACTIVE_INSTALLATION_NAME] = installationName
        }
    }

    suspend fun saveActiveInstallation(installationId: Long, installationName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_INSTALLATION_ID] = installationId
            preferences[KEY_ACTIVE_INSTALLATION_NAME] = installationName
        }
    }

    suspend fun saveActiveRound(roundId: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_ROUND_ID] = roundId
        }
    }

    /**
     * Limpia la información de la ronda activa (al finalizar).
     */
    suspend fun clearActiveSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACTIVE_INSTALLATION_ID)
            preferences.remove(KEY_ACTIVE_ROUND_ID)
            preferences.remove(KEY_ACTIVE_INSTALLATION_NAME)
        }
    }

    /**
     * Recupera el ID de usuario de forma segura (suspendida).
     */
    suspend fun getUserId(): Long? {
        return context.dataStore.data.map { it[KEY_USER_ID] }.first()
    }

    /**
     * Recupera el token de forma segura (suspendida).
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[KEY_TOKEN] }.first()
    }

    /**
     * Recupera la instalación activa de forma segura.
     */
    suspend fun getActiveInstallationId(): Long? {
        return context.dataStore.data.map { it[KEY_ACTIVE_INSTALLATION_ID] }.first()
    }

    suspend fun getActiveInstallationName(): String? {
        return context.dataStore.data.map { it[KEY_ACTIVE_INSTALLATION_NAME] }.first()
    }

    suspend fun getActiveRoundId(): Long? {
        return context.dataStore.data.map { it[KEY_ACTIVE_ROUND_ID] }.first()
    }

    // MÉTODOS SÍNCRONOS (Usar con precaución, pueden causar lag en UI)
    fun getActiveRoundIdSync(): Long? = runBlocking { getActiveRoundId() }
    fun getActiveInstallationIdSync(): Long? = runBlocking { getActiveInstallationId() }
    fun getActiveInstallationNameSync(): String? = runBlocking { getActiveInstallationName() }
    fun getUserIdSync(): Long? = runBlocking { getUserId() }
    fun getTokenSync(): String? = runBlocking { getToken() }
    fun getFullNameSync(): String? = runBlocking { fullNameFlow.first() }

    /**
     * Guarda los datos de la sesión.
     */
    suspend fun saveSession(token: String, role: String, userId: Long, fullName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            preferences[KEY_ROLE] = role
            preferences[KEY_FULL_NAME] = fullName
            preferences[KEY_USER_ID] = userId
        }
    }

    /**
     * Recupera el token como un Flow.
     */
    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    /**
     * Recupera el rol como un Flow.
     */
    val roleFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ROLE] }

    /**
     * Recupera el ID de usuario como un Flow.
     */
    val userIdFlow: Flow<Long?> = context.dataStore.data.map { it[KEY_USER_ID] }

    /**
     * Recupera el nombre completo como un Flow.
     */
    val fullNameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_FULL_NAME] }

    /**
     * Limpia todos los datos de sesión (Logout).
     */
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
