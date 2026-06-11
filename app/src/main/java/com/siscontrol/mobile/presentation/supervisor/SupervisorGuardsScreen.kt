package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.admin.UserCard
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@Composable
fun SupervisorGuardsScreen(
    paddingValues: PaddingValues,
    viewModel: SupervisorGuardsViewModel,
    onCreateGuard: () -> Unit,
    onEditGuard: (Long) -> Unit
) {
    val state by viewModel.state
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Mis Guardias",
            subtitle = "Gestión de personal operativo",
            showAdminLogo = false
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Action Button removed as Supervisors cannot create guards
            Spacer(Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o RUT...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryColor) },
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                val filteredGuards = state.guards.filter {
                    (it.fullName ?: "").contains(searchQuery, ignoreCase = true) || 
                    it.rut?.contains(searchQuery) == true
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredGuards.isEmpty() && searchQuery.isEmpty() && state.error == null) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No tienes guardias asignados.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Solicita a un administrador que asigne guardias a tu cuadrilla.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else if (filteredGuards.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Text(
                                text = "No se encontraron guardias para '$searchQuery'.",
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        item {
                            Text(
                                "Guardias Asignados (${filteredGuards.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        items(filteredGuards) { guard ->
                            UserCard(
                                user = guard,
                                onToggleStatus = null, // Supervisor no puede cambiar estado
                                onRoleChange = null, // Supervisor no puede cambiar roles
                                onEditClick = null // Supervisor no puede editar datos
                            )
                        }
                    }
                }
            }
        }
    }
}
