package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.siscontrol.mobile.presentation.Destinos
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISCard
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCheckpointsScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    token: String,
    role: String,
    onCreateCheckpoint: () -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        // Custom Top Bar with Search
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryVariant)
                .padding(16.dp)
        ) {
            Column {
                Text("Gestión Operativa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    placeholder = { Text("Buscar checkpoint...", color = Color.White.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = PrimaryColor.copy(alpha = 0.3f),
                        unfocusedContainerColor = PrimaryColor.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        // Tabs / Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate(Destinos.adminManagementRoute(token, role)) {
                    popUpTo(Destinos.ADMIN_MANAGEMENT) { inclusive = false }
                } },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Usuarios", color = PrimaryColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { navController.navigate(Destinos.adminInstallationsRoute(token, role)) {
                    popUpTo(Destinos.ADMIN_MANAGEMENT) { inclusive = false }
                } },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Instalaciones", color = PrimaryColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { /* Current Screen */ },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Checkpoints", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Checkpoints (3)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Button(
                        onClick = onCreateCheckpoint,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo", fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Hardcoded Checkpoints
                CheckpointCard(
                    name = "Punto Control Acceso 1",
                    installation = "Bodega Central Norte",
                    nfcCode = "NFC-A1-8899",
                    status = "ACTIVO"
                )
                Spacer(modifier = Modifier.height(12.dp))
                CheckpointCard(
                    name = "Punto Control Recepción",
                    installation = "Bodega Central Norte",
                    nfcCode = "NFC-B2-1122",
                    status = "ACTIVO"
                )
                Spacer(modifier = Modifier.height(12.dp))
                CheckpointCard(
                    name = "Ronda Perimetral Sur",
                    installation = "Edificio Corporativo Sur",
                    nfcCode = "NFC-C3-5566",
                    status = "INACTIVO"
                )
            }
        }
    }
}

@Composable
fun CheckpointCard(
    name: String,
    installation: String,
    nfcCode: String,
    status: String
) {
    SISCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE5E7EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(30.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    if (status == "ACTIVO") {
                        SISBadge(status, containerColor = Color(0xFFD1FAE5), contentColor = SuccessColor)
                    } else {
                        SISBadge(status, containerColor = Color(0xFFFEE2E2), contentColor = DangerColor)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(installation, fontSize = 12.sp, color = TextSecondary)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Nfc, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(nfcCode, fontSize = 12.sp, color = PrimaryColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
