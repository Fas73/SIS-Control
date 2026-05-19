package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siscontrol.mobile.presentation.admin.AdminHomeViewModel
import com.siscontrol.mobile.presentation.admin.KpiCard
import com.siscontrol.mobile.presentation.admin.QuickAccessButton
import com.siscontrol.mobile.presentation.admin.ActiveRoundCard
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase
import com.siscontrol.mobile.di.AppModule

private class SupervisorHomeViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppModule.provideAdminHomeViewModel() as T
}

@Composable
fun SupervisorHomeScreen(
    paddingValues: PaddingValues,
    userName: String,
    onNavigate: (String) -> Unit
) {
    val viewModel: AdminHomeViewModel = viewModel(factory = SupervisorHomeViewModelFactory())
    val state by viewModel.state
    val formattedName = userName.toTitleCase()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Panel Supervisor",
            subtitle = "Hola, $formattedName",
            showAdminLogo = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // KPIs
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Mis Guardias",
                        value = state.totalGuards.toString(),
                        subtitle = "${state.activeShifts} en turno",
                        subtitleColor = SuccessColor,
                        icon = Icons.Default.People,
                        iconColor = PrimaryVariant,
                        iconBg = Color.Transparent
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Alertas",
                        value = state.totalIncidents.toString(),
                        subtitle = "${state.pendingIncidents} pendientes",
                        subtitleColor = DangerColor,
                        icon = Icons.Default.Warning,
                        iconColor = DangerColor,
                        iconBg = Color.Transparent
                    )
                }
            }

            // Quick Access
            item {
                Text("Accesos Rápidos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Ver Mapa",
                        icon = Icons.Default.Map,
                        containerColor = PrimaryColor.copy(alpha = 0.05f),
                        contentColor = PrimaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("MAP") }
                    )
                    QuickAccessButton(
                        title = "Mis Guardias",
                        icon = Icons.Default.Shield,
                        containerColor = SuccessColor.copy(alpha = 0.05f),
                        contentColor = SuccessColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("USERS") }
                    )
                }
            }

            // Active Rounds
            item {
                Text("Rondas en Curso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                if (state.activeRounds.isEmpty()) {
                    Text("No hay actividad reportada en este momento.", color = TextSecondary, fontSize = 14.sp)
                }
            }

            items(state.activeRounds) { round ->
                ActiveRoundCard(
                    guardName = round.guardName,
                    location = round.location,
                    progress = round.progress,
                    progressText = round.progressText,
                    status = round.status,
                    onCancel = { /* El supervisor suele notificar, no cancelar directamente */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
