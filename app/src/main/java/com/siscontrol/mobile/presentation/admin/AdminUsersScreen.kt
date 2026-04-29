package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.siscontrol.mobile.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISBadge
import com.siscontrol.mobile.presentation.components.SISCard
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    paddingValues: PaddingValues,
    onCreateSupervisor: () -> Unit = {},
    onCreateGuard: () -> Unit = {}
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_degrade_sis_control),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Gestión de Usuarios", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    placeholder = { Text("Buscar usuario...", color = Color.White.copy(alpha = 0.6f)) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Supervisores Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Supervisores (2)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Button(
                        onClick = onCreateSupervisor,
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
                
                // Hardcoded Supervisors
                UserCard(
                    name = "Carlos Rodríguez",
                    role = "SUPERVISOR",
                    email = "carlos.r@empresa.com",
                    phone = "+56 9 8765 4321"
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserCard(
                    name = "Ana Morales",
                    role = "SUPERVISOR",
                    email = "ana.m@empresa.com",
                    phone = "+56 9 8765 4322"
                )
            }

            // Guardias Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Guardias (3)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                
                // Hardcoded Guards
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
            }
        }
    }
}

@Composable
fun UserCard(
    name: String,
    role: String,
    email: String,
    phone: String? = null,
    location: String? = null,
    isGuard: Boolean = false
) {
    SISCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE5E7EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(30.dp))
                
                // Status dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(SuccessColor, CircleShape)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    if (isGuard) {
                        SISBadge(role, containerColor = Color(0xFFD1FAE5), contentColor = SuccessColor)
                    } else {
                        SISBadge(role, containerColor = Color(0xFFDBEAFE), contentColor = PrimaryVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(email, fontSize = 12.sp, color = TextSecondary)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (phone != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(phone, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                
                if (location != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
