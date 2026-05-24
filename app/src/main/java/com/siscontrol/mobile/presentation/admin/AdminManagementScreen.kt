package com.siscontrol.mobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.navigation.NavController
import com.siscontrol.mobile.domain.model.User
import com.siscontrol.mobile.presentation.Destinos
import com.siscontrol.mobile.presentation.components.SISCard
import com.siscontrol.mobile.presentation.theme.*
import com.siscontrol.mobile.core.toTitleCase
import com.siscontrol.mobile.core.formatDateToDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    userViewModel: AdminManagementViewModel,
    instViewModel: AdminInstallationsViewModel,
    token: String,
    role: String,
    initialTab: Int = 0
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) } // 0: Usuarios, 1: Instalaciones
    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    // Estados para Usuarios
    var expandedRole by rememberSaveable { mutableStateOf<String?>(null) }
    val userState by userViewModel.state
    
    // Estados para Instalaciones
    val instState by instViewModel.state

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gestión Operativa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Acción Principal Dinámica
                Button(
                    onClick = { 
                        if (selectedTab == 0) {
                            navController.navigate(Destinos.createPersonnelRoute(token, role))
                        } else {
                            navController.navigate(Destinos.adminCreateInstallationRoute(token, role))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (selectedTab == 0) Icons.Default.PersonAdd else Icons.Default.AddBusiness, 
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedTab == 0) "REGISTRAR NUEVO USUARIO" else "REGISTRAR NUEVA INSTALACIÓN", 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buscador Dinámico
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    placeholder = { 
                        Text(
                            if (selectedTab == 0) "Buscar usuario..." else "Buscar instalación...", 
                            color = Color.White.copy(alpha = 0.7f)
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = PrimaryColor.copy(alpha = 0.4f),
                        unfocusedContainerColor = PrimaryColor.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        // Tabs Row (Persistentes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectedTab = 0; searchQuery = "" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) PrimaryColor else Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(40.dp)
            ) {
                Text(
                    "Usuarios", 
                    color = if (selectedTab == 0) Color.White else PrimaryColor, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { selectedTab = 1; searchQuery = "" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) PrimaryColor else Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(40.dp)
            ) {
                Text(
                    "Instalaciones", 
                    color = if (selectedTab == 1) Color.White else PrimaryColor, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Contenido Dinámico
        if (selectedTab == 0) {
            UsersTabContent(userState, searchQuery, userViewModel, expandedRole, navController, token, role) { expandedRole = it }
        } else {
            InstallationsTabContent(instState, searchQuery, navController, token, role)
        }
    }
}

@Composable
fun UsersTabContent(
    state: AdminManagementState,
    searchQuery: String,
    viewModel: AdminManagementViewModel,
    expandedRole: String?,
    navController: NavController,
    token: String,
    role: String,
    onRoleExpand: (String?) -> Unit
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
    } else {
        val filteredUsers = state.users.filter { 
            (it.fullName ?: "").contains(searchQuery, ignoreCase = true) || (it.username ?: "").contains(searchQuery, ignoreCase = true)
        }
        
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        val administrators = filteredUsers.filter { it.role == "ADMIN" }
        val supervisors = filteredUsers.filter { it.role == "SUPERVISOR" }
        val guards = filteredUsers.filter { it.role == "GUARD" || it.role == "GUARDIA" }

        if (state.isActionLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryVariant)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (searchQuery.isEmpty()) {
                // Administradores
                item {
                    RoleSummaryCard(
                        title = "Administradores",
                        count = administrators.size,
                        icon = Icons.Default.AdminPanelSettings,
                        color = PrimaryColor,
                        isExpanded = expandedRole == "ADMIN",
                        onClick = { onRoleExpand(if (expandedRole == "ADMIN") null else "ADMIN") }
                    )
                }
                if (expandedRole == "ADMIN") {
                    items(administrators) { user ->
                        val userId = user.id ?: 0L
                        UserCard(
                            user = user,
                            onToggleStatus = { viewModel.toggleUserStatus(userId) },
                            onRoleChange = { newRole -> viewModel.updateUserRole(user, newRole) },
                            onEditClick = { navController.navigate(Destinos.adminEditUserRoute(userId, token, role)) }
                        )
                    }
                }

                // Supervisores
                item {
                    RoleSummaryCard(
                        title = "Supervisores",
                        count = supervisors.size,
                        icon = Icons.Default.Person,
                        color = WarningColor,
                        isExpanded = expandedRole == "SUPERVISOR",
                        onClick = { onRoleExpand(if (expandedRole == "SUPERVISOR") null else "SUPERVISOR") }
                    )
                }
                if (expandedRole == "SUPERVISOR") {
                    items(supervisors) { user ->
                        val userId = user.id ?: 0L
                        UserCard(
                            user = user,
                            onToggleStatus = { viewModel.toggleUserStatus(userId) },
                            onRoleChange = { newRole -> viewModel.updateUserRole(user, newRole) },
                            onEditClick = { navController.navigate(Destinos.adminEditUserRoute(userId, token, role)) }
                        )
                    }
                }

                // Guardias
                item {
                    RoleSummaryCard(
                        title = "Guardias",
                        count = guards.size,
                        icon = Icons.Default.Shield,
                        color = SuccessColor,
                        isExpanded = expandedRole == "GUARD",
                        onClick = { onRoleExpand(if (expandedRole == "GUARD") null else "GUARD") }
                    )
                }
                if (expandedRole == "GUARD") {
                    items(guards) { user ->
                        val userId = user.id ?: 0L
                        UserCard(
                            user = user,
                            onToggleStatus = { viewModel.toggleUserStatus(userId) },
                            onRoleChange = { newRole -> viewModel.updateUserRole(user, newRole) },
                            onEditClick = { navController.navigate(Destinos.adminEditUserRoute(userId, token, role)) }
                        )
                    }
                }
            } else {
                item {
                    Text("Resultados de búsqueda", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                items(filteredUsers) { user ->
                    val userId = user.id ?: 0L
                    UserCard(
                        user = user,
                        onToggleStatus = { viewModel.toggleUserStatus(userId) },
                        onRoleChange = { newRole -> viewModel.updateUserRole(user, newRole) },
                        onEditClick = { navController.navigate(Destinos.adminEditUserRoute(userId, token, role)) }
                    )
                }
            }
        }
    }
}

@Composable
fun InstallationsTabContent(
    state: InstallationsState,
    searchQuery: String,
    navController: NavController,
    token: String,
    role: String
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
    } else {
        val filteredList = state.installations.filter {
            (it.clientName ?: "").contains(searchQuery, ignoreCase = true) || 
            (it.name ?: "").contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Lista de Empresas (${state.installations.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            items(filteredList) { inst ->
                val checkpointCount = state.checkpointCounts[inst.id ?: 0L] ?: 0
                InstallationSimpleCard(
                    name = inst.clientName ?: inst.name ?: "Sede sin cliente",
                    checkpointCount = checkpointCount,
                    status = inst.status ?: 1,
                    onClick = {
                        navController.navigate(Destinos.adminInstallationDetailRoute(inst.id ?: 0L, token, role))
                    }
                )
            }
        }
    }
}

@Composable
fun RoleSummaryCard(
    title: String, 
    count: Int, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    color: Color,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    SISCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(50.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Total: $count usuarios", fontSize = 14.sp, color = TextSecondary)
                }
            }
            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                contentDescription = null, 
                tint = TextSecondary
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onToggleStatus: () -> Unit,
    onRoleChange: (String) -> Unit,
    onEditClick: () -> Unit
) {
    val isActive = user.status == 1
    var showRoleMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    SISCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Avatar icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFF3F4F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (user.fullName ?: "Sin nombre").toTitleCase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Role Badge
                            val roleStr = user.role ?: "GUARD"
                            val displayRole = when(roleStr.uppercase()) {
                                "ADMIN" -> "Administrador"
                                "SUPERVISOR" -> "Supervisor"
                                "GUARD", "GUARDIA" -> "Guardia"
                                else -> roleStr
                            }
                            
                            Surface(
                                color = if (roleStr.uppercase() == "ADMIN") Color(0xFFEDE9FE) else Color(0xFFD1FAE5),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { showRoleMenu = true }
                            ) {
                                Text(
                                    text = displayRole,
                                    color = if (roleStr.uppercase() == "ADMIN") PrimaryColor else SuccessColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showRoleMenu,
                                onDismissRequest = { showRoleMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Administrador") },
                                    onClick = { onRoleChange("ADMIN"); showRoleMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supervisor") },
                                    onClick = { onRoleChange("SUPERVISOR"); showRoleMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Guardia") },
                                    onClick = { onRoleChange("GUARD"); showRoleMenu = false }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Switch and Status Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessColor,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                    Text(
                        text = if (isActive) "Activo" else "Inactivo",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) SuccessColor else Color.Gray
                    )
                }
            }

            // Expanded Area
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    UserDetailItem(label = "RUT", value = user.rut ?: "N/A")
                    UserDetailItem(label = "Nombre de usuario", value = user.username ?: "N/A")
                    UserDetailItem(label = "Teléfono móvil", value = user.phoneNumber ?: "N/A")
                    UserDetailItem(label = "Fecha de registro", value = (user.createdAt ?: "").formatDateToDisplay())
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("EDITAR DATOS DEL USUARIO", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun InstallationSimpleCard(
    name: String,
    checkpointCount: Int,
    status: Int,
    onClick: () -> Unit
) {
    SISCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (status == 1) PrimaryColor.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        tint = if (status == 1) PrimaryColor else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name, 
                        fontWeight = FontWeight.Bold, 
                        color = TextPrimary, 
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (status == 1) "Activa" else "Inactiva",
                        fontSize = 12.sp,
                        color = if (status == 1) SuccessColor else DangerColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Contador de Checkpoints "Hermoseado"
            Surface(
                color = PrimaryVariant.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryVariant.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp), 
                        tint = PrimaryVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$checkpointCount", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = PrimaryVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (checkpointCount == 1) "Punto" else "Puntos", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Medium, 
                        color = PrimaryVariant
                    )
                }
            }
        }
    }
}
