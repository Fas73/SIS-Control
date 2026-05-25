package com.siscontrol.mobile.di

import android.content.Context
import com.siscontrol.mobile.data.remote.*
import com.siscontrol.mobile.data.repository.*
import com.siscontrol.mobile.domain.usecase.*
import com.siscontrol.mobile.presentation.login.LoginViewModel
import com.siscontrol.mobile.presentation.management.CreatePersonnelViewModel
import com.siscontrol.mobile.presentation.admin.AdminInstallationsViewModel
import com.siscontrol.mobile.presentation.admin.AdminManagementViewModel
import com.siscontrol.mobile.presentation.admin.AdminCheckpointsViewModel
import com.siscontrol.mobile.presentation.admin.AdminHomeViewModel
import com.siscontrol.mobile.presentation.admin.AdminMapViewModel
import com.siscontrol.mobile.presentation.admin.AdminAlertsViewModel
import com.siscontrol.mobile.presentation.guard.IncidentViewModel
import com.siscontrol.mobile.presentation.guard.GuardInstallationsViewModel
import com.siscontrol.mobile.presentation.guard.GuardRoundViewModel
import com.siscontrol.mobile.presentation.guard.GuardHistoryViewModel
import com.siscontrol.mobile.presentation.profile.ProfileViewModel
import com.siscontrol.mobile.presentation.supervisor.SupervisorGuardsViewModel
import com.siscontrol.mobile.presentation.management.CreateCheckpointViewModel
import com.siscontrol.mobile.presentation.admin.log.AdminIncidentLogViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.siscontrol.mobile.core.Config

object AppModule {

    // CAMBIO AQUÍ: En lugar de un String fijo, usamos el de tu archivo Config
    val BASE_URL = Config.BASE_URL

    private lateinit var sessionManager: SessionManager
    private lateinit var database: com.siscontrol.mobile.data.local.AppDatabase

    private val _unauthorizedEvent = MutableSharedFlow<Unit>()
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    fun init(context: Context) {
        sessionManager = SessionManager(context)
        database = com.siscontrol.mobile.data.local.AppDatabase.getDatabase(context)
    }


    fun getSessionManager() = sessionManager
    fun getDatabase() = database

    // -------------------------------------------------------------------------
    // Network Stack
    // -------------------------------------------------------------------------

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val originalRequest = chain.request()
                val token = sessionManager.getTokenSync()

                val newRequest = if (!token.isNullOrBlank()) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }

                val response: Response = chain.proceed(newRequest)

                if (response.code == 401) {
                    runBlocking {
                        sessionManager.clearSession()
                        _unauthorizedEvent.emit(Unit)
                    }
                }
                response
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // -------------------------------------------------------------------------
    // API Services
    // -------------------------------------------------------------------------

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val personnelApiService: PersonnelApiService by lazy { retrofit.create(PersonnelApiService::class.java) }
    val installationApiService: InstallationApiService by lazy { retrofit.create(InstallationApiService::class.java) }
    val attendanceApiService: AttendanceApiService by lazy { retrofit.create(AttendanceApiService::class.java) }
    val roundApiService: RoundApiService by lazy { retrofit.create(RoundApiService::class.java) }
    val reportApiService: ReportApiService by lazy { retrofit.create(ReportApiService::class.java) }
    val profileApiService: ProfileApiService by lazy { retrofit.create(ProfileApiService::class.java) }
    val incidentApiService: IncidentApiService by lazy { retrofit.create(IncidentApiService::class.java) }

    // -------------------------------------------------------------------------
    // Repositories
    // -------------------------------------------------------------------------

    private val authRepository by lazy { AuthRepositoryImpl(authApiService) }
    private val personnelRepository by lazy { PersonnelRepositoryImpl(personnelApiService) }
    private val installationRepository by lazy { InstallationRepositoryImpl(installationApiService) }
    private val attendanceRepository by lazy { AttendanceRepositoryImpl(attendanceApiService) }
    private val roundRepository by lazy { RoundRepositoryImpl(roundApiService) }
    private val reportRepository by lazy { ReportRepositoryImpl(reportApiService) }
    private val profileRepository by lazy { ProfileRepositoryImpl(profileApiService) }
    private val incidentRepository by lazy { IncidentRepositoryImpl(incidentApiService) }

    // -------------------------------------------------------------------------
    // Use Cases
    // -------------------------------------------------------------------------

    private val loginUseCase by lazy { LoginUseCase(authRepository) }

    // Personnel
    val getPersonnelUseCase by lazy { GetPersonnelUseCase(personnelRepository) }
    val getUserByIdUseCase by lazy { GetUserByIdUseCase(personnelRepository) }
    val createPersonnelUseCase by lazy { CreatePersonnelUseCase(personnelRepository) }
    val updatePersonnelUseCase by lazy { UpdatePersonnelUseCase(personnelRepository) }
    val toggleUserStatusUseCase by lazy { ToggleUserStatusUseCase(personnelRepository) }

    // Installations & Checkpoints
    val getInstallationsUseCase by lazy { GetInstallationsUseCase(installationRepository) }
    val createInstallationUseCase by lazy { CreateInstallationUseCase(installationRepository) }
    val updateInstallationUseCase by lazy { UpdateInstallationUseCase(installationRepository) }
    val toggleInstallationStatusUseCase by lazy { ToggleInstallationStatusUseCase(installationRepository) }
    val getCheckpointsUseCase by lazy { GetCheckpointsUseCase(installationRepository) }
    val createCheckpointUseCase by lazy { CreateCheckpointUseCase(installationRepository) }
    val updateCheckpointUseCase by lazy { UpdateCheckpointUseCase(installationRepository) }
    val toggleCheckpointStatusUseCase by lazy { ToggleCheckpointStatusUseCase(installationRepository) }

    // Attendance & Rounds
    val getAllShiftsUseCase by lazy { GetAllShiftsUseCase(attendanceRepository) }
    val getAllRoundsUseCase by lazy { GetAllRoundsUseCase(roundRepository) }
    val checkInUseCase by lazy { CheckInUseCase(attendanceRepository) }
    val checkOutUseCase by lazy { CheckOutUseCase(attendanceRepository) }
    val startRoundUseCase by lazy { StartRoundUseCase(roundRepository) }
    val endRoundUseCase by lazy { EndRoundUseCase(roundRepository) }
    val getCurrentGuardStateUseCase by lazy { GetCurrentGuardStateUseCase(roundRepository) }
    val getRoundDetailUseCase by lazy { GetRoundDetailUseCase(roundRepository) }
    val scanCheckpointUseCase by lazy { ScanCheckpointUseCase(roundRepository) }
    val cancelRoundUseCase by lazy { CancelRoundUseCase(roundRepository) }
    val cancelShiftUseCase by lazy { CancelShiftUseCase(roundRepository) }

    // Reports
    val getAdminDashboardUseCase by lazy { GetAdminDashboardUseCase(reportRepository) }
    val getGuardRoundsHistoryUseCase by lazy { GetGuardRoundsHistoryUseCase(reportRepository) }
    val generateCsvReportUseCase by lazy { GenerateCsvReportUseCase(reportRepository) }

    // Profile
    val updateProfileDataUseCase by lazy { UpdateProfileDataUseCase(profileRepository) }
    val changeMyPasswordUseCase by lazy { ChangeMyPasswordUseCase(profileRepository) }

    // Incidents
    val reportIncidentUseCase by lazy { ReportIncidentUseCase(incidentRepository) }
    val triggerPanicUseCase by lazy { TriggerPanicUseCase(incidentRepository) }

    // -------------------------------------------------------------------------
    // ViewModel Factories
    // -------------------------------------------------------------------------

    fun provideLoginViewModel(): LoginViewModel =
        LoginViewModel(loginUseCase)

    fun provideAdminHomeViewModel(): AdminHomeViewModel =
        AdminHomeViewModel(
            getAdminDashboardUseCase,
            cancelRoundUseCase,
            cancelShiftUseCase,
            generateCsvReportUseCase,
            sessionManager
        )

    fun provideCreatePersonnelViewModel(): CreatePersonnelViewModel =
        CreatePersonnelViewModel(createPersonnelUseCase, getInstallationsUseCase, getPersonnelUseCase, sessionManager)

    fun provideAdminInstallationsViewModel(): AdminInstallationsViewModel =
        AdminInstallationsViewModel(
            getInstallationsUseCase = getInstallationsUseCase,
            createInstallationUseCase = createInstallationUseCase,
            updateInstallationUseCase = updateInstallationUseCase,
            toggleInstallationStatusUseCase = toggleInstallationStatusUseCase,
            getCheckpointsUseCase = getCheckpointsUseCase,
            createCheckpointUseCase = createCheckpointUseCase,
            updateCheckpointUseCase = updateCheckpointUseCase,
            toggleCheckpointStatusUseCase = toggleCheckpointStatusUseCase,
            sessionManager = sessionManager
        )

    fun provideAdminManagementViewModel(): AdminManagementViewModel =
        AdminManagementViewModel(
            getPersonnelUseCase = getPersonnelUseCase,
            getUserByIdUseCase = getUserByIdUseCase,
            updatePersonnelUseCase = updatePersonnelUseCase,
            toggleUserStatusUseCase = toggleUserStatusUseCase,
            sessionManager = sessionManager
        )

    fun provideSupervisorGuardsViewModel(): SupervisorGuardsViewModel =
        SupervisorGuardsViewModel(
            getPersonnelUseCase,
            toggleUserStatusUseCase,
            sessionManager
        )

    fun provideProfileViewModel(): ProfileViewModel =
        ProfileViewModel(
            getUserByIdUseCase = getUserByIdUseCase,
            checkOutUseCase = checkOutUseCase,
            updateProfileDataUseCase = updateProfileDataUseCase,
            changeMyPasswordUseCase = changeMyPasswordUseCase,
            sessionManager = sessionManager
        )

    fun provideAdminCheckpointsViewModel(): AdminCheckpointsViewModel =
        AdminCheckpointsViewModel(getCheckpointsUseCase, getInstallationsUseCase)

    fun provideAdminAlertsViewModel(): AdminAlertsViewModel =
        AdminAlertsViewModel(incidentRepository)

    fun provideAdminIncidentLogViewModel(): AdminIncidentLogViewModel =
        AdminIncidentLogViewModel(incidentRepository)

    fun provideAdminMapViewModel(): AdminMapViewModel =
        AdminMapViewModel(getAdminDashboardUseCase)

    fun provideGuardInstallationsViewModel(): GuardInstallationsViewModel =
        GuardInstallationsViewModel(
            getInstallationsUseCase = getInstallationsUseCase,
            getCheckpointsUseCase = getCheckpointsUseCase,
            checkInUseCase = checkInUseCase,
            checkOutUseCase = checkOutUseCase,
            getCurrentGuardStateUseCase = getCurrentGuardStateUseCase,
            startRoundUseCase = startRoundUseCase,
            sessionManager = sessionManager
        )

    fun provideGuardHomeViewModel(): com.siscontrol.mobile.presentation.guard.GuardHomeViewModel =
        com.siscontrol.mobile.presentation.guard.GuardHomeViewModel(getCurrentGuardStateUseCase, sessionManager)

    fun provideGuardRoundViewModel(): GuardRoundViewModel =
        GuardRoundViewModel(
            endRoundUseCase = endRoundUseCase,
            getCheckpointsUseCase = getCheckpointsUseCase,
            getRoundDetailUseCase = getRoundDetailUseCase,
            scanCheckpointUseCase = scanCheckpointUseCase,
            triggerPanicUseCase = triggerPanicUseCase,
            sessionManager = sessionManager
        )

    fun provideGuardHistoryViewModel(): GuardHistoryViewModel =
        GuardHistoryViewModel(
            getGuardRoundsHistoryUseCase = getGuardRoundsHistoryUseCase,
            sessionManager = sessionManager
        )

    fun provideIncidentViewModel(): IncidentViewModel =
        IncidentViewModel(reportIncidentUseCase)

    fun provideCreateCheckpointViewModel(): CreateCheckpointViewModel =
        CreateCheckpointViewModel(createCheckpointUseCase, sessionManager)
}
