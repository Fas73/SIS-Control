package com.siscontrol.mobile.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userRole: String, // Recibe el rol del usuario logueado (ADMIN, SUPERVISOR o GUARDIA)
    onNavigateToPersonnel: () -> Unit,
    onNavigateToCreatePersonnel: () -> Unit, // Nuevo: pantalla de registro de personal
    onNavigateToInstallations: () -> Unit,
    onNavigateToAddCheckpoint: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryColor, PrimaryVariant)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, shape = MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = PrimaryColor, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "SIS Control",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = PrimaryColor
            ) {
                // Inicio (Todos los roles)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { /* Ya estamos en el inicio, en un futuro manejar estado interno si es necesario */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryColor,
                        selectedTextColor = PrimaryColor,
                        indicatorColor = Color(0xFFDBEAFE) // Light blue
                    )
                )

                if (userRole == "ADMIN" || userRole == "SUPERVISOR") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Personal") },
                        label = { Text("Personal") },
                        selected = false,
                        onClick = onNavigateToPersonnel,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Registrar") },
                        label = { Text("Registrar") },
                        selected = false,
                        onClick = onNavigateToCreatePersonnel,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }

                // Perfil / Logout (Todos los roles)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Salir") },
                    label = { Text("Salir") },
                    selected = false,
                    onClick = onLogout,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}
