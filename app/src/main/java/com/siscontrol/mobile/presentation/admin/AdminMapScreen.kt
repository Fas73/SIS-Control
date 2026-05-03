package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.BackgroundColor
import com.siscontrol.mobile.presentation.theme.PrimaryColor
import com.siscontrol.mobile.presentation.theme.TextSecondary

@Composable
fun AdminMapScreen(
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Mapa en Vivo",
            subtitle = "Ubicación de personal e instalaciones",
            showAdminLogo = true
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = PrimaryColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Módulo de Mapa (Próximamente)",
                    fontSize = 18.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
