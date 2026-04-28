package com.siscontrol.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.siscontrol.mobile.presentation.AppNavigation
import com.siscontrol.mobile.presentation.theme.SISControlTheme

/**
 * Punto de entrada único de la aplicación Android.
 *
 * Responsabilidad: arrancar el sistema de navegación (NavHost) dentro del tema visual.
 * Toda la lógica de pantallas, ViewModels y navegación vive en [AppNavigation].
 *
 * Anotación importante:
 *   - No hacemos DI aquí directamente; [AppModule] actúa como contenedor manual.
 *   - El grafo de navegación maneja el ciclo de vida de los ViewModels a través de
 *     viewModel() factories, lo que garantiza que se creen y destruyan correctamente.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SISControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Lanza el grafo completo de navegación:
                    //   LoginScreen -> MainScreen -> (PersonnelList | CreatePersonnel | ...)
                    AppNavigation()
                }
            }
        }
    }
}
