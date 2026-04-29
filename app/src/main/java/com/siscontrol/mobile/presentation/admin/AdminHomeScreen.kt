package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    paddingValues: PaddingValues,
    onNavigate: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Panel de Control",
            subtitle = "Administración Central",
            showAdminLogo = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    placeholder = { Text("Buscar instalaciones, personal o alertas...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                Text(
                    "Resumen Operativo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Instalaciones",
                        value = "12",
                        subtitle = "Todas activas",
                        icon = Icons.Default.LocationCity,
                        iconColor = PrimaryVariant,
                        iconBg = PrimaryVariant.copy(alpha = 0.1f)
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Personal",
                        value = "45",
                        subtitle = "En turno hoy",
                        icon = Icons.Default.Group,
                        iconColor = SuccessColor,
                        iconBg = SuccessColor.copy(alpha = 0.1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                KpiCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Alertas Críticas",
                    value = "3",
                    subtitle = "Requieren atención inmediata",
                    subtitleColor = DangerColor,
                    icon = Icons.Default.Warning,
                    iconColor = DangerColor,
                    iconBg = DangerColor.copy(alpha = 0.1f)
                )
            }

            item {
                Text(
                    "Accesos Rápidos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessButton(
                        title = "Mapa",
                        icon = Icons.Default.Map,
                        containerColor = PrimaryVariant.copy(alpha = 0.05f),
                        contentColor = PrimaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("MAP") }
                    )
                    QuickAccessButton(
                        title = "Alertas",
                        icon = Icons.Default.NotificationsActive,
                        containerColor = DangerColor.copy(alpha = 0.05f),
                        contentColor = DangerColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("ALERTS") }
                    )
                    QuickAccessButton(
                        title = "Usuarios",
                        icon = Icons.Default.People,
                        containerColor = SuccessColor.copy(alpha = 0.05f),
                        contentColor = SuccessColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("USERS") }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rondas Activas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Ver todas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryColor,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                ActiveRoundCard(
                    guardName = "Juan Pérez",
                    location = "Plaza Centro",
                    progress = 0.6f,
                    progressText = "60% completado",
                    status = "En Ronda"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ActiveRoundCard(
                    guardName = "María González",
                    location = "Bodega Norte",
                    progress = 0.3f,
                    progressText = "30% completado",
                    status = "En Ronda"
                )
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color = TextSecondary,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = subtitleColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ActiveRoundCard(
    guardName: String,
    location: String,
    progress: Float,
    progressText: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(guardName.first().toString(), color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(guardName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(location, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
                SISBadge(status, containerColor = PrimaryVariant.copy(alpha = 0.15f), contentColor = PrimaryColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = PrimaryVariant,
                trackColor = Color(0xFFF3F4F6)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(progressText, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun QuickAccessButton(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
