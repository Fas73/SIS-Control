package com.siscontrol.mobile.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import com.siscontrol.mobile.R
import com.siscontrol.mobile.presentation.theme.BackgroundColor
import com.siscontrol.mobile.presentation.theme.PrimaryColor
import com.siscontrol.mobile.presentation.theme.TextPrimary
import com.siscontrol.mobile.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * SplashScreen — Pantalla de carga inicial de SIS-Control.
 *
 * Muestra el logo oficial, el nombre de la app y una barra de progreso simulada.
 * Al completar la carga, navega automáticamente a [LoginScreen].
 *
 * Diseñada para ser stateless y fácilmente extensible:
 * - En el futuro, [onNavigateToLogin] puede reemplazarse por [onSplashComplete]
 *   que reciba el resultado de una validación de sesión/token almacenado.
 *
 * @param onNavigateToLogin Callback ejecutado al terminar la carga simulada.
 *                          En producción: reemplazar por lógica de validación de sesión.
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit
) {
    // ── Animación de escala y opacidad del logo ──────────────────────────────
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val alphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Entrada suave del contenido
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
        )
        // Barra de progreso simulada (800 ms)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
        // Pequeña pausa antes de navegar
        delay(200)

        // TODO (producción): Antes de navegar, verificar token persistido en DataStore.
        //   val session = sessionManager.getSession()
        //   if (session.isValid) onNavigateToDashboard(session.token, session.role)
        //   else onNavigateToLogin()
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo ────────────────────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.logo_branding_sis_control),
                contentDescription = "Logo SIS-Control",
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Nombre de la App ────────────────────────────────────────────
            Text(
                text = "SIS-Control",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sistema de Control de Seguridad",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Barra de progreso simulada ──────────────────────────────────
            LinearProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp),
                color = PrimaryColor,
                trackColor = PrimaryColor.copy(alpha = 0.15f)
            )
        }

        // ── Versión en la parte inferior ────────────────────────────────────
        Text(
            text = "v1.0.0-beta",
            color = TextSecondary.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
