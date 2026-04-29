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
import androidx.navigation.navArgument
import com.siscontrol.mobile.di.AppModule
import com.siscontrol.mobile.presentation.login.LoginScreen
import com.siscontrol.mobile.presentation.login.LoginViewModel
import com.siscontrol.mobile.presentation.main.HomeContent
import com.siscontrol.mobile.presentation.main.MainScreen
import com.siscontrol.mobile.presentation.management.CreatePersonnelScreen
import com.siscontrol.mobile.presentation.management.CreatePersonnelViewModel
import com.siscontrol.mobile.presentation.management.PersonnelListScreen

// ---------------------------------------------------------------------------
// Destinos de Navegación
// ---------------------------------------------------------------------------

/**
 * Objeto que centraliza todos los destinos (rutas) de la app.
 * Usar constantes evita errores de typo y facilita el refactor.
 */
object Destinos {
    const val LOGIN = "login"
    const val MAIN  = "main/{token}/{role}"
    const val PERSONNEL_LIST    = "personnel_list/{token}/{role}"
    const val CREATE_PERSONNEL  = "create_personnel/{token}/{role}"

    // Nuevas Rutas Figma
    const val ADMIN_HOME = "admin_home/{token}/{role}"
    const val ADMIN_USERS = "admin_users/{token}/{role}"
    const val ADMIN_CREATE_SUPERVISOR = "admin_create_supervisor/{token}/{role}"
    const val ADMIN_CREATE_GUARD = "admin_create_guard/{token}/{role}"
    const val ADMIN_MAP = "admin_map/{token}/{role}"
    const val ADMIN_ALERTS = "admin_alerts/{token}/{role}"
    const val SUPERVISOR_HOME = "supervisor_home/{token}/{role}"
    const val SUPERVISOR_GUARDS = "supervisor_guards/{token}/{role}"
    const val SUPERVISOR_MAP = "supervisor_map/{token}/{role}"
    const val SUPERVISOR_ALERTS = "supervisor_alerts/{token}/{role}"
    const val GUARD_HOME = "guard_home/{token}/{role}"
    const val GUARD_PROFILE = "guard_profile/{token}/{role}"
    const val GUARD_RONDA = "guard_ronda/{token}/{role}"
    const val GUARD_HISTORY = "guard_history/{token}/{role}"
    const val GUARD_INCIDENT = "guard_incident/{token}/{role}"
    const val GUARD_NFC = "guard_nfc/{token}/{role}"

    fun mainRoute(token: String, role: String) = "main/${encode(token)}/${encode(role)}"
    fun personnelListRoute(token: String, role: String) = "personnel_list/${encode(token)}/${encode(role)}"
    fun createPersonnelRoute(token: String, role: String) = "create_personnel/${encode(token)}/${encode(role)}"

    fun adminHomeRoute(token: String, role: String) = "admin_home/${encode(token)}/${encode(role)}"
    fun adminUsersRoute(token: String, role: String) = "admin_users/${encode(token)}/${encode(role)}"
    fun adminCreateSupervisorRoute(token: String, role: String) = "admin_create_supervisor/${encode(token)}/${encode(role)}"
    fun adminCreateGuardRoute(token: String, role: String) = "admin_create_guard/${encode(token)}/${encode(role)}"
    fun adminMapRoute(token: String, role: String) = "admin_map/${encode(token)}/${encode(role)}"
    fun adminAlertsRoute(token: String, role: String) = "admin_alerts/${encode(token)}/${encode(role)}"
    fun supervisorHomeRoute(token: String, role: String) = "supervisor_home/${encode(token)}/${encode(role)}"
    fun supervisorGuardsRoute(token: String, role: String) = "supervisor_guards/${encode(token)}/${encode(role)}"
    fun supervisorMapRoute(token: String, role: String) = "supervisor_map/${encode(token)}/${encode(role)}"
    fun supervisorAlertsRoute(token: String, role: String) = "supervisor_alerts/${encode(token)}/${encode(role)}"
    fun guardHomeRoute(token: String, role: String) = "guard_home/${encode(token)}/${encode(role)}"
    fun guardProfileRoute(token: String, role: String) = "guard_profile/${encode(token)}/${encode(role)}"
    fun guardRondaRoute(token: String, role: String) = "guard_ronda/${encode(token)}/${encode(role)}"
    fun guardHistoryRoute(token: String, role: String) = "guard_history/${encode(token)}/${encode(role)}"
    fun guardIncidentRoute(token: String, role: String) = "guard_incident/${encode(token)}/${encode(role)}"
    fun guardNfcRoute(token: String, role: String) = "guard_nfc/${encode(token)}/${encode(role)}"

    private fun encode(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
}

// ---------------------------------------------------------------------------
// Factories manuales para ViewModels con parámetros (sin Hilt)
// ---------------------------------------------------------------------------

private class LoginViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AppModule.provideLoginViewModel() as T
}

private class CreatePersonnelViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AppModule.provideCreatePersonnelViewModel() as T
}

// ---------------------------------------------------------------------------
// NavGraph principal
// ---------------------------------------------------------------------------

/**
 * Grafo de navegación completo de la aplicación.
 *
 * Flujo:
 *   LoginScreen
 *     └─► MainScreen  (con menú lateral según rol)
 *           ├─► PersonnelListScreen
 *           └─► CreatePersonnelScreen
 *
 * El token JWT y el rol del usuario autenticado se pasan como argumentos
 * de ruta entre destinos, de modo que cualquier pantalla que necesite hacer
 * llamadas autenticadas pueda obtenerlos del NavBackStackEntry.
 *
 * NOTA: En producción se usaría un DataStore o SessionManager compartido.
 * Para la demo funcional de este prototipo, los argumentos de ruta son
 * suficientes y evitan la necesidad de Hilt o un Singleton global de sesión.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val sessionManager = AppModule.getSessionManager()

    // Observar eventos de "No Autorizado" (401) globalmente
    LaunchedEffect(Unit) {
        AppModule.unauthorizedEvent.collect {
            // Si el interceptor detecta un 401, redirigimos al Login inmediatamente
            navController.navigate(Destinos.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Destinos.LOGIN) {

        // ------------------------------------------------------------------
        // 1. Pantalla de Login
        // ------------------------------------------------------------------
        composable(Destinos.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory())

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { token, role ->
                    // Guardar sesión en DataStore de forma persistente
                    scope.launch {
                        sessionManager.saveSession(token, role)
                        // Navegar al Home específico según el rol
                        val nextRoute = when (role) {
                            "ADMIN" -> Destinos.adminHomeRoute(token, role)
                            "SUPERVISOR" -> Destinos.supervisorHomeRoute(token, role)
                            "GUARDIA" -> Destinos.guardHomeRoute(token, role)
                            else -> Destinos.mainRoute(token, role)
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Destinos.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ------------------------------------------------------------------
        // 2. Pantalla Principal (con menú lateral)
        // ------------------------------------------------------------------
        // Pantalla Principal (Scaffold de transición o fallback)
        composable(
            route = Destinos.MAIN,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("role")  { type = NavType.StringType }
            )
        ) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role")  ?: "", "UTF-8")

            // Redirección por si acaso llega aquí
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

        // ------------------------------------------------------------------
        // ADMIN Screens
        // ------------------------------------------------------------------
        composable(Destinos.ADMIN_HOME, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.admin.AdminHomeScreen(
                    paddingValues = padding,
                    onNavigate = { target ->
                        val route = when(target) {
                            "MAP" -> Destinos.adminMapRoute(token, role)
                            "ALERTS" -> Destinos.adminAlertsRoute(token, role)
                            "USERS" -> Destinos.adminUsersRoute(token, role)
                            else -> Destinos.adminHomeRoute(token, role)
                        }
                        navController.navigate(route)
                    }
                )
            }
        }

        composable(Destinos.ADMIN_USERS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.admin.AdminUsersScreen(
                    paddingValues = padding,
                    onCreateSupervisor = { navController.navigate(Destinos.adminCreateSupervisorRoute(token, role)) },
                    onCreateGuard = { navController.navigate(Destinos.adminCreateGuardRoute(token, role)) }
                )
            }
        }

        composable(Destinos.ADMIN_CREATE_SUPERVISOR, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            com.siscontrol.mobile.presentation.admin.CreateSupervisorScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }

        composable(Destinos.ADMIN_CREATE_GUARD, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            com.siscontrol.mobile.presentation.supervisor.CreateGuardScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }

        // ------------------------------------------------------------------
        // SUPERVISOR Screens
        // ------------------------------------------------------------------
        composable(Destinos.SUPERVISOR_HOME, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.supervisor.SupervisorHomeScreen(
                    paddingValues = padding,
                    onNavigate = { target ->
                        val route = when(target) {
                            "MAP" -> Destinos.supervisorMapRoute(token, role)
                            "ALERTS" -> Destinos.supervisorAlertsRoute(token, role)
                            "USERS" -> Destinos.supervisorGuardsRoute(token, role)
                            else -> Destinos.supervisorHomeRoute(token, role)
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
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.supervisor.SupervisorGuardsScreen(
                    paddingValues = padding,
                    onCreateGuard = { navController.navigate(Destinos.adminCreateGuardRoute(token, role)) }
                )
            }
        }

        // ------------------------------------------------------------------
        // GUARDIA Screens
        // ------------------------------------------------------------------
        composable(Destinos.GUARD_HOME, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.guard.GuardHomeScreen(
                    paddingValues = padding,
                    onNavigate = { target ->
                        val route = when(target) {
                            "RONDA" -> Destinos.guardRondaRoute(token, role)
                            "HISTORY" -> Destinos.guardHistoryRoute(token, role)
                            else -> Destinos.guardHomeRoute(token, role)
                        }
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
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.guard.GuardProfileScreen(
                    paddingValues = padding,
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

        // ------------------------------------------------------------------
        // PLACEHOLDER SCREENS (New Figma Flows)
        // ------------------------------------------------------------------
        
        composable(Destinos.ADMIN_MAP, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.admin.AdminMapScreen(paddingValues = padding)
            }
        }

        composable(Destinos.ADMIN_ALERTS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.admin.AdminAlertsScreen(paddingValues = padding)
            }
        }

        composable(Destinos.SUPERVISOR_MAP, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                PlaceholderScreen("Mapa de Guardias (SUPERVISOR)", padding)
            }
        }

        composable(Destinos.SUPERVISOR_ALERTS, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                PlaceholderScreen("Centro de Alertas (SUPERVISOR)", padding)
            }
        }

        composable(Destinos.GUARD_RONDA, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                com.siscontrol.mobile.presentation.guard.GuardiaRondaActivaScreen(
                    onFinishRound = { navController.popBackStack() },
                    onReportIncident = { navController.navigate(Destinos.guardIncidentRoute(token, role)) },
                    onPanic = { /* Handle Panic */ }
                )
            }
        }

        composable(Destinos.GUARD_HISTORY, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                PlaceholderScreen("Historial de Rondas (GUARDIA)", padding)
            }
        }

        composable(Destinos.GUARD_INCIDENT, arguments = listOf(
            navArgument("token") { type = NavType.StringType },
            navArgument("role")  { type = NavType.StringType }
        )) { backStack ->
            val token = java.net.URLDecoder.decode(backStack.arguments?.getString("token") ?: "", "UTF-8")
            val role  = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")
            com.siscontrol.mobile.presentation.main.MainScaffold(navController, role, token) { padding ->
                PlaceholderScreen("Reportar Incidente (GUARDIA)", padding)
            }
        }

        // ------------------------------------------------------------------
        // 3. Lista de Personal
        // ------------------------------------------------------------------
        composable(
            route = Destinos.PERSONNEL_LIST,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("role")  { type = NavType.StringType }
            )
        ) { backStack ->
            val role = java.net.URLDecoder.decode(backStack.arguments?.getString("role") ?: "", "UTF-8")

            PersonnelListScreen(
                userRole = role,
                onBack = { navController.popBackStack() }
            )
        }

        // ------------------------------------------------------------------
        // 4. Crear Personal (Supervisor o Guardia según rol)
        // ------------------------------------------------------------------
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

@Composable
fun PlaceholderScreen(name: String, padding: androidx.compose.foundation.layout.PaddingValues) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "$name\nPróximamente",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}
