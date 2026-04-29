package com.siscontrol.mobile.presentation.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.admin.UserCard
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorGuardsScreen(
    paddingValues: PaddingValues,
    onCreateGuard: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).background(PrimaryColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("SIS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Gestión de Guardias", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    placeholder = { Text("Buscar guardia...", color = Color.White.copy(alpha = 0.6f)) },
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
                    Text("Mis Guardias (5)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Button(
                        onClick = onCreateGuard,
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
                
                // Hardcoded Guards for Demo
                UserCard(
                    name = "Juan Pérez",
                    role = "GUARDIA",
                    email = "juan.p@empresa.com",
                    location = "Plaza Centro",
                    isGuard = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserCard(
                    name = "María González",
                    role = "GUARDIA",
                    email = "maria.g@empresa.com",
                    location = "Bodega Norte",
                    isGuard = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserCard(
                    name = "Pedro Sánchez",
                    role = "GUARDIA",
                    email = "pedro.s@empresa.com",
                    location = "Edificio Sur",
                    isGuard = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserCard(
                    name = "Ana Torres",
                    role = "GUARDIA",
                    email = "ana.t@empresa.com",
                    location = "Plaza Centro",
                    isGuard = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserCard(
                    name = "Luis Ramírez",
                    role = "GUARDIA",
                    email = "luis.r@empresa.com",
                    location = "Bodega Norte",
                    isGuard = true
                )
            }
        }
    }
}
