package com.siscontrol.mobile.di

import com.siscontrol.mobile.data.remote.AuthApiService
import com.siscontrol.mobile.data.remote.InstallationApiService
import com.siscontrol.mobile.data.remote.PersonnelApiService
import com.siscontrol.mobile.data.repository.AuthRepositoryImpl
import com.siscontrol.mobile.data.repository.InstallationRepositoryImpl
import com.siscontrol.mobile.data.repository.PersonnelRepositoryImpl
import com.siscontrol.mobile.domain.usecase.CreatePersonnelUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import com.siscontrol.mobile.domain.usecase.GetPersonnelUseCase
import com.siscontrol.mobile.domain.usecase.LoginUseCase
import com.siscontrol.mobile.presentation.login.LoginViewModel
import com.siscontrol.mobile.presentation.management.CreatePersonnelViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking

/**
 * Módulo de Inyección de Dependencias manual (sin Hilt/Dagger).
 *
 * Actúa como el único punto de ensamblaje de la aplicación:
 *   Retrofit → ApiService → RepositoryImpl → UseCase → ViewModel
 *
 * La URL base apunta al backend Spring Boot corriendo en el emulador
 * de Android (10.0.2.2 es el alias que el emulador usa para el localhost del PC).
 * Si usas un dispositivo físico, reemplaza esta IP por la IP local de tu PC.
 */
object AppModule {

    /** URL del backend. Ajusta el puerto si tu Spring Boot no usa el 8080. */
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Gestor de sesión persistente (DataStore)
    private lateinit var sessionManager: SessionManager
    
    // Canal de eventos para notificar errores 401 a la UI
    private val _unauthorizedEvent = MutableSharedFlow<Unit>()
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    /**
     * Inicializa el módulo con el contexto de la aplicación.
     */
    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    fun getSessionManager() = sessionManager

    // -------------------------------------------------------------------------
    // OkHttpClient — con Interceptor de Autenticación y Manejo de 401
    // -------------------------------------------------------------------------

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val originalRequest = chain.request()
                val token = sessionManager.getTokenSync()

                val newRequest = if (!token.isNullOrBlank()) {
                    Log.d("SISControlAPI", "Inyectando JWT en petición a: ${originalRequest.url()}")
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }

                val response: Response = chain.proceed(newRequest)

                // Si detectamos un 401, limpiamos la sesión y notificamos a la app
                if (response.code() == 401) {
                    Log.e("SISControlAPI", "Sesión expirada o inválida (401). Cerrando sesión...")
                    runBlocking {
                        sessionManager.clearSession()
                        _unauthorizedEvent.emit(Unit)
                    }
                } else if (response.isSuccessful) {
                    Log.d("SISControlAPI", "Respuesta exitosa de: ${originalRequest.url()}")
                }

                response
            }
            .build()
    }

    // -------------------------------------------------------------------------
    // Retrofit — cliente HTTP único compartido por toda la app
    // -------------------------------------------------------------------------

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <--- Usamos el cliente con interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // -------------------------------------------------------------------------
    // API Services
    // -------------------------------------------------------------------------

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val personnelApiService: PersonnelApiService by lazy {
        retrofit.create(PersonnelApiService::class.java)
    }

    val installationApiService: InstallationApiService by lazy {
        retrofit.create(InstallationApiService::class.java)
    }

    // -------------------------------------------------------------------------
    // Repositories (implementaciones concretas de las interfaces del Dominio)
    // -------------------------------------------------------------------------

    private val authRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }

    private val personnelRepository by lazy {
        PersonnelRepositoryImpl(personnelApiService)
    }

    private val installationRepository by lazy {
        InstallationRepositoryImpl(installationApiService)
    }

    // -------------------------------------------------------------------------
    // Use Cases
    // -------------------------------------------------------------------------

    private val loginUseCase         by lazy { LoginUseCase(authRepository) }
    val getPersonnelUseCase          by lazy { GetPersonnelUseCase(personnelRepository) }
    val createPersonnelUseCase       by lazy { CreatePersonnelUseCase(personnelRepository) }
    val getInstallationsUseCase      by lazy { GetInstallationsUseCase(installationRepository) }

    // -------------------------------------------------------------------------
    // ViewModel Factories
    // -------------------------------------------------------------------------

    /**
     * Crea una nueva instancia de [LoginViewModel].
     */
    fun provideLoginViewModel(): LoginViewModel =
        LoginViewModel(loginUseCase)

    /**
     * Crea una nueva instancia de [CreatePersonnelViewModel].
     */
    fun provideCreatePersonnelViewModel(): CreatePersonnelViewModel =
        CreatePersonnelViewModel(createPersonnelUseCase, getInstallationsUseCase)
}
