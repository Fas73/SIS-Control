package com.siscontrol.mobile

import android.app.Application
import com.siscontrol.mobile.di.AppModule

/**
 * Clase Application personalizada para la aplicación SIS Control.
 * 
 * Se utiliza para centralizar la inicialización de componentes globales,
 * como la inyección de dependencias manual (AppModule) y el gestor de sesiones.
 */
class SISControlApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicializamos el AppModule pasándole el contexto de la aplicación.
        // Esto permite que el SessionManager (DataStore) se inicialice correctamente.
        AppModule.init(this)
    }
}
