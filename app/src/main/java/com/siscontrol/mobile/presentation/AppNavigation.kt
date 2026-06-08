package com.siscontrol.mobile.presentation

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navArgument
import com.siscontrol.mobile.di.AppModule
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.presentation.guard.GuardRoundViewModel
import com.siscontrol.mobile.presentation.login.ForgotPasswordScreen
import com.siscontrol.mobile.presentation.login.ForgotPasswordViewModel
import com.siscontrol.mobile.presentation.login.LoginScreen
import com.siscontrol.mobile.presentation.login.LoginViewModel
import com.siscontrol.mobile.presentation.main.MainScreen
import com.siscontrol.mobile.presentation.management.CreatePersonnelScreen
import com.siscontrol.mobile.presentation.management.CreatePersonnelViewModel
import com.siscontrol.mobile.presentation.management.PersonnelListScreen
import com.siscontrol.mobile.presentation.splash.SplashScreen
import com.siscontrol.mobile.presentation.admin.AdminInstallationsViewModel
import com.siscontrol.mobile.presentation.admin.AdminManagementViewModel
import com.siscontrol.mobile.presentation.admin.AdminAlertsViewModel
import com.siscontrol.mobile.presentation.AdminAlertsViewModelFactory
import com.siscontrol.mobile.presentation.admin.AdminCheckpointsViewModel
import com.siscontrol.mobile.presentation.admin.AdminHomeViewModel
import com.siscontrol.mobile.presentation.admin.AdminHomeScreen
import com.siscontrol.mobile.presentation.admin.AdminManagementScreen
import com.siscontrol.mobile.presentation.admin.InstallationDetailScreen
import com.siscontrol.mobile.presentation.admin.AdminCheckpointsScreen
import com.siscontrol.mobile.presentation.admin.AdminMapViewModel
import com.siscontrol.mobile.presentation.admin.EditUserScreen
import com.siscontrol.mobile.presentation.admin.CreateInstallationScreen
import com.siscontrol.mobile.presentation.admin.CreateCheckpointScreen
import com.siscontrol.mobile.presentation.admin.CreateSupervisorScreen
import com.siscontrol.mobile.presentation.admin.AdminMapScreen
import com.siscontrol.mobile.presentation.admin.AdminAlertsScreen
import com.siscontrol.mobile.presentation.supervisor.SupervisorHomeScreen
import com.siscontrol.mobile.presentation.supervisor.SupervisorGuardsScreen
import com.siscontrol.mobile.presentation.supervisor.CreateGuardScreen
import com.siscontrol.mobile.presentation.admin.log.AdminIncidentLogScreen
import com.siscontrol.mobile.presentation.admin.log.AdminIncidentLogViewModel
import com.siscontrol.mobile.presentation.guard.*
import com.siscontrol.mobile.presentation.main.MainScaffold

// ---------------------------------------------------------------------------
// Destinos de Navegación
// ---------------------------------------------------------------------------

object Destinos {
    const val SPLASH          = "splash"
    const val LOGIN           = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    
    const val MAIN  = "main/{token}/{role}"
    const val PERSONNEL_LIST    = "personnel_list/{token}/{role}"
    const val CREATE_PERSONNEL  = "create_personnel/{token}/{role}"

    const val ADMIN_HOME = "admin_home/{token}/{role}"
    const val ADMIN_MANAGEMENT = "admin_management/{token}/{role}"
    const val ADMIN_INSTALLATIONS = "admin_installations/{token}/{role}"
    const val ADMIN_CHECKPOINTS = "admin_checkpoints/{token}/{role}"
    const val ADMIN_CREATE_INSTALLATION = "admin_create_installation/{token}/{role}"
    const val ADMIN_CREATE_CHECKPOINT = "admin_create_checkpoint/{token}/{role}/{installationId}/{installationName}"
    const val ADMIN_INSTALLATION_DETAIL = "admin_installation_detail/{id}/{token}/{role}"
    const val ADMIN_MAP = "admin_map/{token}/{role}"
    const val ADMIN_ALERTS = "admin_alerts/{token}/{role}"
    const val ADMIN_EDIT_USER = "admin_edit_user/{userId}/{token}/{role}"
    const val ADMIN_INCIDENT_LOG = "admin_incident_log/{token}/{role}"

    const val SUPERVISOR_HOME = "supervisor_home/{token}/{role}"
    const val SUPERVISOR_GUARDS = "supervisor_guards/{token}/{role}"
    const val SUPERVISOR_MAP = "supervisor_map/{token}/{role}"
    const val SUPERVISOR_ALERTS = "supervisor_alerts/{token}/{role}"

    const val GUARD_HOME = "guard_home/{token}/{role}"
    const val GUARD_PROFILE = "guard_profile/{token}/{role}"
    const val GUARD_START_ROUND = "guard_start_round/{token}/{role}"
    const val GUARD_RONDA = "guard_ronda/{token}/{role}/{roundId}/{installationId}/{installationName}"
    const val GUARD_HISTORY = "guard_history/{token}/{role}"
    const val GUARD_ROUND_DETAIL = "guard_round_detail/{roundId}/{token}/{role}"
    const val GUARD_INCIDENT = "guard_incident/{token}/{role}"
    const val GUARD_NFC = "guard_nfc/{token}/{role}"
    const val GUARD_CHECKPOINT = "guard_checkpoint/{token}/{role}"
    const val GUARD_CHECKPOINT_CONFIRM = "guard_checkpoint_confirm/{token}/{role}"

    fun mainRoute(token: String, role: String) = "main/${encode(token)}/${encode(role)}"
    fun personnelListRoute(token: String, role: String) = "personnel_list/${encode(token)}/${encode(role)}"
    fun createPersonnelRoute(token: String, role: String) = "create_personnel/${encode(token)}/${encode(role)}"

    fun adminHomeRoute(token: String, role: String) = "admin_home/${encode(token)}/${encode(role)}"
    fun adminManagementRoute(token: String, role: String) = "admin_management/${encode(token)}/${encode(role)}"
    fun adminInstallationsRoute(token: String, role: String) = "admin_installations/${encode(token)}/${encode(role)}"
    fun adminCheckpointsRoute(token: String, role: String) = "admin_checkpoints/${encode(token)}/${encode(role)}"
    fun adminCreateInstallationRoute(token: String, role: String) = "admin_create_installation/${encode(token)}/${encode(role)}"
    fun adminInstallationDetailRoute(id: Long, token: String, role: String) = "admin_installation_detail/$id/${encode(token)}/${encode(role)}"
    fun adminCreateCheckpointRoute(token: String, role: String, installationId: Long, installationName: String) = 
        "admin_create_checkpoint/${encode(token)}/${encode(role)}/$installationId/${encode(installationName)}"
    fun adminMapRoute(token: String, role: String) = "admin_map/${encode(token)}/${encode(role)}"
    fun adminIncidentLogRoute(token: String, role: String) = "admin_incident_log/${encode(token)}/${encode(role)}"
    fun adminAlertsRoute(token: String, role: String) = "admin_alerts/${encode(token)}/${encode(role)}"
    fun adminEditUserRoute(userId: Long, token: String, role: String) = "admin_edit_user/$userId/${encode(token)}/${encode(role)}"
    fun supervisorHomeRoute(token: String, role: String) = "supervisor_home/${encode(token)}/${encode(role)}"
    fun supervisorGuardsRoute(token: String, role: String) = "supervisor_guards/${encode(token)}/${encode(role)}"
    fun supervisorMapRoute(token: String, role: String) = "supervisor_map/${encode(token)}/${encode(role)}"
    fun supervisorAlertsRoute(token: String, role: String) = "supervisor_alerts/${encode(token)}/${encode(role)}"
    fun guardHomeRoute(token: String, role: String) = "guard_home/${encode(token)}/${encode(role)}"
    fun guardProfileRoute(token: String, role: String) = "guard_profile/${encode(token)}/${encode(role)}"
    fun guardStartRoundRoute(token: String, role: String) = "guard_start_round/${encode(token)}/${encode(role)}"
    fun guardRondaRoute(token: String, role: String, roundId: Long, installationId: Long, installationName: String) = 
        "guard_ronda/${encode(token)}/${encode(role)}/$roundId/$installationId/${encode(installationName)}"
    fun guardHistoryRoute(token: String, role: String) = "guard_history/${encode(token)}/${encode(role)}"
    fun guardRoundDetailRoute(roundId: Long, token: String, role: String) = 
        "guard_round_detail/$roundId/${encode(token)}/${encode(role)}"
    fun guardIncidentRoute(token: String, role: String) = "guard_incident/${encode(token)}/${encode(role)}"
    fun guardNfcRoute(token: String, role: String) = "guard_nfc/${encode(token)}/${encode(role)}"
    fun guardCheckpointRoute(token: String, role: String) = "guard_checkpoint/${encode(token)}/${encode(role)}"
    fun guardCheckpointConfirmRoute(token: String, role: String) = "guard_checkpoint_confirm/${encode(token)}/${encode(role)}"

    private fun encode(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
}

// ---------------------------------------------------------------------------
// Factories manuales
// ---------------------------------------------------------------------------

private class LoginViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideLoginViewModel() as T
}

private class ForgotPasswordViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideForgotPasswordViewModel() as T
}

private class AdminHomeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminHomeViewModel() as T
}

private class CreatePersonnelViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideCreatePersonnelViewModel() as T
}

private class AdminInstallationsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminInstallationsViewModel() as T
}

private class AdminManagementViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminManagementViewModel() as T
}

private class AdminCheckpointsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminCheckpointsViewModel() as T
}

private class AdminAlertsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminAlertsViewModel() as T
}

class AdminMapViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminMapViewModel() as T
}

private class SupervisorGuardsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideSupervisorGuardsViewModel() as T
}

private class GuardHomeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideGuardHomeViewModel() as T
}

private class GuardInstallationsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideGuardInstallationsViewModel() as T
}

private class GuardRoundViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideGuardRoundViewModel() as T
}

private class GuardHistoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideGuardHistoryViewModel() as T
}

private class IncidentViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideIncidentViewModel() as T
}

private class ProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideProfileViewModel() as T
}

private class AdminIncidentLogViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminIncidentLogViewModel() as T
}

// ---------------------------------------------------------------------------
// NavGraph principal
// ---------------------------------------------------------------------------

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val sessionManager = AppModule.getSessionManager()

    LaunchedEffect(Unit) {
        AppModule.unauthorizedEvent.collect {
            navController.navigate(Destinos.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController, 
        startDestination = Destinos.SPLASH,
        enterTransition = {
            androidx.compose.animation.slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
        },
        exitTransition = {
            androidx.compose.animation.slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
        },
        popEnterTransition = {
            androidx.compose.animation.slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
        },
        popExitTransition = {
            androidx.compose.animation.slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
        }
    ) {

        composable(Destinos.SPLASH) {
            SplashScreen(onNavigateToLogin = {
                navController.navigate(Destinos.LOGIN) {
                    popUpTo(Destinos.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Destinos.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory())
            LoginScreen(
                viewModel = loginViewModel,
                onForgotPassword = { navController.navigate(Destinos.FORGOT_PASSWORD) },
                onLoginSuccess = { token, role, userId, fullName, username ->
                    scope.launch {
                        // Guardamos en DataStore
                        sessionManager.saveSession(token, role, userId, fullName)
                        
                        // También guardamos en Room para asegurar que getEditorId() funcione correctamente
                        AppModule.getDatabase().userSessionDao().insertSession(
                            com.siscontrol.mobile.data.local.entities.UserSessionEntity(
                                id = userId,
                                username = username,
                                fullName = fullName,
                                role = role,
                                status = "Active"
                            )
                        )

                        val nextRoute = when (role.uppercase()) {
                            "ADMIN" -> Destinos.adminHomeRoute(token, role)
                            "SUPERVISOR" -> Destinos.supervisorHomeRoute(token, role)
                            "GUARD", "GUARDIA" -> Destinos.guardHomeRoute(token, role)
                            else -> Destinos.mainRoute(token, role)
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Destinos.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Destinos.FORGOT_PASSWORD) {
            val forgotViewModel: ForgotPasswordViewModel = viewModel(factory = ForgotPasswordViewModelFactory())
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                viewModel = forgotViewModel
            )
        }

        composable(
            route = Destinos.MAIN,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("role")  { type = NavType.StringType }
            )
        ) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role")  ?: "", "UTF-8")

            LaunchedEffect(Unit) {
                val nextRoute = when (role) {
                    "ADMIN" -> Destinos.adminHomeRoute(token, role)
                    "SUPERVISOR" -> Destinos.supervisorHomeRoute(token, role)
                    "GUARDIA" -> Destinos.guardHomeRoute(token, role)
                    else -> null
                }
                if (nextRoute != null) {
                    navController.navigate(nextRoute) {
                        popUpTo(Destinos.MAIN) { inclusive = true }
                    }
                }
            }
        }

        // ── ADMIN Screens ──────────────────────────────────────────────────
        composable(Destinos.ADMIN_HOME, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val fullName by sessionManager.fullNameFlow.collectAsState(initial = "Cargando...")
            val vm: AdminHomeViewModel = viewModel(factory = AdminHomeViewModelFactory())
            
            // Recargar datos cada vez que se entra al Home
            LaunchedEffect(Unit) { vm.loadDashboardData() }

            MainScaffold(navController, role, token) { padding ->
                AdminHomeScreen(
                    paddingValues = padding,
                    userName = fullName ?: "Usuario",
                    viewModel = vm,
                    token = token,
                    role = role,
                    onNavigate = { target ->
                        val route = when(target) {
                            "MAP" -> Destinos.adminMapRoute(token, role)
                            "ALERTS" -> Destinos.adminAlertsRoute(token, role)
                            "MANAGEMENT" -> Destinos.adminManagementRoute(token, role)
                            else -> target // Permitir rutas completas directas
                        }
                        navController.navigate(route)
                    }
                )
            }
        }

        composable(Destinos.ADMIN_MANAGEMENT, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            
            val userVm: AdminManagementViewModel = viewModel(factory = AdminManagementViewModelFactory())
            val instVm: AdminInstallationsViewModel = viewModel(factory = AdminInstallationsViewModelFactory())
            
            LaunchedEffect(Unit) { 
                userVm.loadUsers()
                instVm.loadInstallations()
            }

            MainScaffold(navController, role, token) { padding ->
                AdminManagementScreen(
                    paddingValues = padding,
                    navController = navController,
                    userViewModel = userVm,
                    instViewModel = instVm,
                    token = token,
                    role = role
                )
            }
        }

        // Redirigir la antigua ruta de instalaciones a la Gestión Operativa unificada
        composable(Destinos.ADMIN_INSTALLATIONS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            
            val userVm: AdminManagementViewModel = viewModel(factory = AdminManagementViewModelFactory())
            val instVm: AdminInstallationsViewModel = viewModel(factory = AdminInstallationsViewModelFactory())
            
            LaunchedEffect(Unit) { 
                userVm.loadUsers()
                instVm.loadInstallations()
            }

            MainScaffold(navController, role, token) { padding ->
                // Iniciamos con la pestaña 1 (Instalaciones)
                AdminManagementScreen(
                    paddingValues = padding,
                    navController = navController,
                    userViewModel = userVm,
                    instViewModel = instVm,
                    token = token,
                    role = role,
                    initialTab = 1
                )
            }
        }

        composable(Destinos.ADMIN_CHECKPOINTS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: AdminCheckpointsViewModel = viewModel(factory = AdminCheckpointsViewModelFactory())
            LaunchedEffect(Unit) { vm.loadCheckpoints(null) }
            MainScaffold(navController, role, token) { padding ->
                AdminCheckpointsScreen(
                    paddingValues = padding,
                    navController = navController,
                    viewModel = vm,
                    token = token,
                    role = role,
                    onCreateCheckpoint = { 
                        // Como quitamos la pestaña, este botón ya no debería usarse para creación general,
                        // pero lo dejamos funcional apuntando a una instalación genérica o vacío
                        navController.navigate(Destinos.adminCreateCheckpointRoute(token, role, 0L, "General"))
                    }
                )
            }
        }

        composable(Destinos.ADMIN_CREATE_INSTALLATION, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) {
            val vm: AdminInstallationsViewModel = viewModel(factory = AdminInstallationsViewModelFactory())
            CreateInstallationScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.ADMIN_INSTALLATION_DETAIL,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("token") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: 0L
            val token = backStack.arguments?.getString("token") ?: ""
            val role = backStack.arguments?.getString("role") ?: ""
            val vm: AdminInstallationsViewModel = viewModel(factory = AdminInstallationsViewModelFactory())
            InstallationDetailScreen(
                navController = navController,
                token = token,
                role = role,
                installationId = id,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinos.ADMIN_CREATE_CHECKPOINT, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType },
            navArgument("installationId") { type = NavType.LongType },
            navArgument("installationName") { type = NavType.StringType }
        )) { backStack ->
            val installationId = backStack.arguments?.getLong("installationId") ?: 0L
            val installationName = java.net.URLDecoder.decode(backStack.arguments?.getString("installationName") ?: "", "UTF-8")
            val vm: AdminInstallationsViewModel = viewModel(factory = AdminInstallationsViewModelFactory())
            
            CreateCheckpointScreen(
                installationId = installationId,
                predefinedInstallationName = installationName,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.ADMIN_EDIT_USER,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("token") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStack ->
            val userId = backStack.arguments?.getLong("userId") ?: 0L
            val token = backStack.arguments?.getString("token") ?: ""
            val role = backStack.arguments?.getString("role") ?: ""
            val vm: AdminManagementViewModel = viewModel(factory = AdminManagementViewModelFactory())
            
            EditUserScreen(
                userId = userId,
                viewModel = vm,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ── SUPERVISOR Screens ─────────────────────────────────────────────
        composable(Destinos.SUPERVISOR_HOME, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val fullName by sessionManager.fullNameFlow.collectAsState(initial = "Cargando...")

            MainScaffold(navController, role, token) { padding ->
                SupervisorHomeScreen(
                    paddingValues = padding,
                    userName = fullName ?: "Supervisor",
                    token = token,
                    role = role,
                    onNavigate = { target ->
                        val route = when(target) {
                            "MAP" -> Destinos.supervisorMapRoute(token, role)
                            "ALERTS" -> Destinos.supervisorAlertsRoute(token, role)
                            "USERS" -> Destinos.supervisorGuardsRoute(token, role)
                            else -> target
                        }
                        navController.navigate(route)
                    }
                )
            }
        }

        composable(Destinos.SUPERVISOR_GUARDS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: com.siscontrol.mobile.presentation.supervisor.SupervisorGuardsViewModel = 
                viewModel(factory = SupervisorGuardsViewModelFactory())
            
            LaunchedEffect(Unit) { vm.loadGuards() }

            MainScaffold(navController, role, token) { padding ->
                SupervisorGuardsScreen(
                    paddingValues = padding,
                    viewModel = vm,
                    onCreateGuard = { navController.navigate(Destinos.createPersonnelRoute(token, role)) },
                    onEditGuard = { userId ->
                        navController.navigate(Destinos.adminEditUserRoute(userId, token, role))
                    }
                )
            }
        }

        composable(Destinos.SUPERVISOR_MAP, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: AdminMapViewModel = viewModel(factory = AdminMapViewModelFactory())
            
            MainScaffold(navController, role, token) { padding ->
                AdminMapScreen(paddingValues = padding, viewModel = vm)
            }
        }

        composable(Destinos.SUPERVISOR_ALERTS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: AdminAlertsViewModel = viewModel(factory = AdminAlertsViewModelFactory())
            
            MainScaffold(navController, role, token) { padding ->
                AdminAlertsScreen(paddingValues = padding, viewModel = vm)
            }
        }

        // ── GUARDIA Screens ───────────────────────────────────────────────
        composable(Destinos.GUARD_HOME, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val fullName by sessionManager.fullNameFlow.collectAsState(initial = "Cargando...")
            val vm: GuardHomeViewModel = viewModel(factory = GuardHomeViewModelFactory())
            val instVm: GuardInstallationsViewModel = viewModel(factory = GuardInstallationsViewModelFactory())

            MainScaffold(navController, role, token) { padding ->
                GuardHomeScreen(
                    paddingValues = padding,
                    userName = fullName ?: "Guardia",
                    viewModel = vm,
                    instViewModel = instVm,
                    token = token,
                    role = role,
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }
        }

        composable(Destinos.GUARD_PROFILE, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: com.siscontrol.mobile.presentation.profile.ProfileViewModel = 
                viewModel(factory = ProfileViewModelFactory())

            MainScaffold(navController, role, token) { padding ->
                GuardProfileScreen(
                    paddingValues = padding,
                    viewModel = vm,
                    onLogout = {
                        scope.launch {
                            sessionManager.clearSession()
                            navController.navigate(Destinos.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Destinos.GUARD_START_ROUND,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("role")  { type = NavType.StringType }
            )
        ) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: GuardInstallationsViewModel =
                viewModel(factory = GuardInstallationsViewModelFactory())

            MainScaffold(navController, role, token) { padding ->
                GuardStartRoundScreen(
                    paddingValues = padding,
                    viewModel = vm,
                    onBack = { 
                        // Regresamos al Home de forma segura, limpiando el stack anterior.
                        navController.navigate(Destinos.guardHomeRoute(token, role)) {
                            popUpTo(Destinos.GUARD_HOME) { inclusive = true }
                        }
                    },
                    onStartRound = { rId: Long, iId: Long, iName: String -> 
                        navController.navigate(Destinos.guardRondaRoute(token, role, rId, iId, iName))
                    }
                )
            }
        }

        composable(Destinos.GUARD_RONDA, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType },
            navArgument("roundId") { type = NavType.LongType },
            navArgument("installationId") { type = NavType.LongType },
            navArgument("installationName") { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val roundId = backStack.arguments?.getLong("roundId") ?: 0L
            val installationId = backStack.arguments?.getLong("installationId") ?: 0L
            val installationName = java.net.URLDecoder.decode(backStack.arguments?.getString("installationName") ?: "Instalación", "UTF-8")
            
            val vm: GuardRoundViewModel = viewModel(factory = GuardRoundViewModelFactory())

            LaunchedEffect(installationId, roundId) {
                vm.loadCheckpoints(installationId, roundId)
            }

            MainScaffold(navController, role, token) { padding ->
                GuardiaRondaActivaScreen(
                    paddingValues = padding,
                    roundId = roundId,
                    installationName = installationName,
                    viewModel = vm,
                    onFinishRound = { 
                        navController.navigate(Destinos.guardHomeRoute(token, role)) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onReportIncident = { navController.navigate("guard_incident_dynamic/$roundId") },
                    onPanic = { },
                    onScanCheckpoint = { checkpoint, scanned, total ->
                        navController.navigate("guard_checkpoint_confirm_dynamic/${checkpoint.id}/${checkpoint.name}/${checkpoint.executionOrder}/${checkpoint.instruction ?: "Ninguna"}/$scanned/$total/${java.net.URLEncoder.encode(installationName, "UTF-8")}")
                    }
                )
            }
        }

        composable(
            route = "guard_incident_dynamic/{roundId}",
            arguments = listOf(navArgument("roundId") { type = NavType.LongType })
        ) { backStack ->
            val roundId = backStack.arguments?.getLong("roundId") ?: 0L
            val vm: IncidentViewModel = viewModel(factory = IncidentViewModelFactory())
            GuardReportIncidentScreen(
                roundExecutionId = roundId,
                viewModel = vm,
                onSaveSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = "guard_checkpoint_confirm_dynamic/{id}/{name}/{order}/{instruction}/{scanned}/{total}/{instName}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType },
                navArgument("order") { type = NavType.IntType },
                navArgument("instruction") { type = NavType.StringType },
                navArgument("scanned") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("instName") { type = NavType.StringType }
            )
        ) { backStack ->
            val name = backStack.arguments?.getString("name") ?: ""
            val order = backStack.arguments?.getInt("order") ?: 0
            val instruction = backStack.arguments?.getString("instruction")
            val scanned = backStack.arguments?.getInt("scanned") ?: 0
            val total = backStack.arguments?.getInt("total") ?: 0
            val instName = java.net.URLDecoder.decode(backStack.arguments?.getString("instName") ?: "Instalación", "UTF-8")
            
            GuardCheckpointConfirmScreen(
                checkpointName = name,
                checkpointNumber = order,
                installationName = instName,
                instruction = if (instruction == "Ninguna") null else instruction,
                completedCheckpoints = scanned,
                totalCheckpoints = total,
                onContinue = { navController.popBackStack() }
            )
        }

        composable(Destinos.GUARD_HISTORY, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: GuardHistoryViewModel = viewModel(factory = GuardHistoryViewModelFactory())

            MainScaffold(navController, role, token) { padding ->
                GuardHistoryScreen(
                    paddingValues = padding, 
                    viewModel = vm,
                    onNavigateToDetail = { rId ->
                        navController.navigate(Destinos.guardRoundDetailRoute(rId, token, role))
                    }
                )
            }
        }

        composable(
            route = Destinos.GUARD_ROUND_DETAIL,
            arguments = listOf(
                navArgument("roundId") { type = NavType.LongType },
                navArgument("token") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStack ->
            val roundId = backStack.arguments?.getLong("roundId") ?: 0L
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: GuardRoundViewModel = viewModel(factory = GuardRoundViewModelFactory())

            GuardRoundDetailScreen(
                roundId = roundId,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinos.ADMIN_INCIDENT_LOG, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: AdminIncidentLogViewModel = viewModel(factory = AdminIncidentLogViewModelFactory())

            MainScaffold(navController, role, token) { padding ->
                AdminIncidentLogScreen(
                    paddingValues = padding,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Destinos.ADMIN_MAP, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: AdminMapViewModel = viewModel(factory = AdminMapViewModelFactory())
            
            MainScaffold(navController, role, token) { padding ->
                AdminMapScreen(paddingValues = padding, viewModel = vm)
            }
        }

        composable(Destinos.ADMIN_ALERTS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: AdminAlertsViewModel = viewModel(factory = AdminAlertsViewModelFactory())
            
            MainScaffold(navController, role, token) { padding ->
                AdminAlertsScreen(paddingValues = padding, viewModel = vm)
            }
        }

        composable(Destinos.GUARD_INCIDENT, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) {
            val roundId = sessionManager.getActiveRoundIdSync() ?: 0L
            val vm: IncidentViewModel = viewModel(factory = IncidentViewModelFactory())
            GuardReportIncidentScreen(
                roundExecutionId = roundId,
                viewModel = vm,
                onSaveSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Destinos.GUARD_CHECKPOINT, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            MainScaffold(navController, role, token) {
                GuardCheckpointScreen(
                    onScanSuccess = { tagId ->
                        navController.navigate(Destinos.guardCheckpointConfirmRoute(token, role))
                    },
                    onSimulateScan = { navController.navigate(Destinos.guardCheckpointConfirmRoute(token, role)) }
                )
            }
        }

        composable(Destinos.GUARD_CHECKPOINT_CONFIRM, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            MainScaffold(navController, role, token) {
                GuardCheckpointConfirmScreen(
                    onContinue = {
                        navController.popBackStack()
                        navController.popBackStack()
                    }
                )
            }
        }

        // ── 3. Lista de Personal ──────────────────────────────────────────
        composable(
            route = Destinos.PERSONNEL_LIST,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("role")  { type = NavType.StringType }
            )
        ) { backStack ->
            val role = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            PersonnelListScreen(userRole = role, onBack = { navController.popBackStack() })
        }

        // ── 4. Crear Personal ─────────────────────────────────────────────
        composable(
            route = Destinos.CREATE_PERSONNEL,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("role")  { type = NavType.StringType }
            )
        ) { backStack ->
            val role = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            val vm: CreatePersonnelViewModel = viewModel(factory = CreatePersonnelViewModelFactory())
            CreatePersonnelScreen(
                viewModel = vm,
                currentUserRole = role,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
