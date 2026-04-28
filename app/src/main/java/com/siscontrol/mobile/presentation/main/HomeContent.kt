package com.siscontrol.mobile.presentation.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeContent(
    padding: PaddingValues,
    role: String
) {
    val modifier = Modifier.padding(padding)
    
    when (role) {
        "ADMIN" -> AdminHomeScreen(modifier = modifier)
        "SUPERVISOR" -> SupervisorHomeScreen(modifier = modifier)
        "GUARDIA" -> GuardiaHomeScreen(modifier = modifier)
        else -> AdminHomeScreen(modifier = modifier) // Default fallback
    }
}
