package com.siscontrol.mobile.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.ButtonVariant
import com.siscontrol.mobile.presentation.components.PrimaryButton
import com.siscontrol.mobile.presentation.theme.*

@Composable
fun GuardiaHomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Botón principal de Iniciar Ronda
        PrimaryButton(
            text = "Iniciar Ronda",
            onClick = { /* TODO: Navegar a iniciar ronda */ },
            modifier = Modifier.height(80.dp),
            variant = ButtonVariant.PRIMARY
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Mi Actividad",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                KpiCard("Rondas Hoy", "2", PrimaryVariant)
            }
            item {
                KpiCard("Puntos Control", "12", SuccessColor)
            }
        }
    }
}
