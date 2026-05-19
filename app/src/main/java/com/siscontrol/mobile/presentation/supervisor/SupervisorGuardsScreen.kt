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
    onCreateGuard: () -> Unit
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
            // Action Button
            Button(
                onClick = onCreateGuard,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("REGISTRAR NUEVO GUARDIA", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o RUT...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryColor) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                val filteredGuards = state.guards.filter {
                    it.fullName.contains(searchQuery, ignoreCase = true) || 
                    it.rut?.contains(searchQuery) == true
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            onToggleStatus = { viewModel.toggleGuardStatus(guard.id) },
                            onRoleChange = { /* Supervisor no puede cambiar roles */ },
                            onEditClick = { /* TODO: Navegar a editar */ }
                        )
                    }
                }
            }
        }
    }
}
