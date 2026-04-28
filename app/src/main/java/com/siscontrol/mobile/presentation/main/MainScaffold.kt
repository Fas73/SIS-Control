package com.siscontrol.mobile.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.siscontrol.mobile.presentation.Destinos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.*

// TODO: Ensure R.drawable icons exist or use Material Icons
// We will use standard Material Icons or define basic ones for now

@Composable
fun MainScaffold(
    navController: NavHostController,
    userRole: String,
    token: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val items = getBottomNavItemsForRole(userRole, token)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Only show bottom bar if there are items for the role
            if (items.isNotEmpty()) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute?.startsWith(item.baseRoute) == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content(innerPadding)
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val baseRoute: String,
    val icon: ImageVector
)

fun getBottomNavItemsForRole(role: String, token: String): List<BottomNavItem> {
    return when (role) {
        "ADMIN" -> listOf(
            BottomNavItem("Inicio", Destinos.adminHomeRoute(token, role), Destinos.ADMIN_HOME, Icons.Default.Home),
            BottomNavItem("Mapa", Destinos.adminMapRoute(token, role), Destinos.ADMIN_MAP, Icons.Default.Map),
            BottomNavItem("Alertas", Destinos.adminAlertsRoute(token, role), Destinos.ADMIN_ALERTS, Icons.Default.Warning),
            BottomNavItem("Usuarios", Destinos.adminUsersRoute(token, role), Destinos.ADMIN_USERS, Icons.Default.Group),
            BottomNavItem("Perfil", Destinos.guardProfileRoute(token, role), Destinos.GUARD_PROFILE, Icons.Default.Person)
        )
        "SUPERVISOR" -> listOf(
            BottomNavItem("Inicio", Destinos.supervisorHomeRoute(token, role), Destinos.SUPERVISOR_HOME, Icons.Default.Home),
            BottomNavItem("Mapa", Destinos.supervisorMapRoute(token, role), Destinos.SUPERVISOR_MAP, Icons.Default.Map),
            BottomNavItem("Alertas", Destinos.supervisorAlertsRoute(token, role), Destinos.SUPERVISOR_ALERTS, Icons.Default.Warning),
            BottomNavItem("Guardias", Destinos.supervisorGuardsRoute(token, role), Destinos.SUPERVISOR_GUARDS, Icons.Default.Group),
            BottomNavItem("Perfil", Destinos.guardProfileRoute(token, role), Destinos.GUARD_PROFILE, Icons.Default.Person)
        )
        "GUARDIA" -> listOf(
            BottomNavItem("Inicio", Destinos.guardHomeRoute(token, role), Destinos.GUARD_HOME, Icons.Default.Home),
            BottomNavItem("Ronda", Destinos.guardRondaRoute(token, role), Destinos.GUARD_RONDA, Icons.Default.RadioButtonChecked),
            BottomNavItem("Historial", Destinos.guardHistoryRoute(token, role), Destinos.GUARD_HISTORY, Icons.Default.History),
            BottomNavItem("Perfil", Destinos.guardProfileRoute(token, role), Destinos.GUARD_PROFILE, Icons.Default.Person)
        )
        else -> emptyList()
    }
}
