package com.siscontrol.mobile.core

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.siscontrol.mobile.data.remote.dto.IncidentDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage

object StompService {

    private var mStompClient: StompClient? = null
    private val gson = Gson()
    
    private val _adminAlertFlow = MutableSharedFlow<IncidentDto>()
    val adminAlertFlow = _adminAlertFlow.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("CheckResult")
    fun connect(baseUrl: String) {
        try {
            // Limpieza preventiva si ya existe una instancia corrupta o previa
            mStompClient?.disconnect()
            
            val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "ws/websocket"
            Log.d("STOMP", "Iniciando conexión segura a: $wsUrl")

            // Usamos try-catch interno para la creación del cliente (punto común de NoClassDefFoundError)
            mStompClient = try {
                Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
            } catch (t: Throwable) {
                Log.e("STOMP", "No se pudo crear el cliente STOMP. Verifique dependencias RxJava/OkHttp.", t)
                null
            }

            if (mStompClient == null) return

            mStompClient?.lifecycle()?.subscribe({ lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> Log.d("STOMP", "¡Conexión abierta!")
                    LifecycleEvent.Type.ERROR -> Log.e("STOMP", "Error en socket", lifecycleEvent.exception)
                    LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "Conexión cerrada")
                    else -> {}
                }
            }, { error ->
                Log.e("STOMP", "Error en el flujo de ciclo de vida", error)
            })

            mStompClient?.connect()

            // Suscribirse al canal de alertas de forma segura
            mStompClient?.topic("/topic/alertas")?.subscribe({ topicMessage: StompMessage ->
                val payload = topicMessage.payload
                try {
                    val alert = gson.fromJson(payload, IncidentDto::class.java)
                    scope.launch {
                        _adminAlertFlow.emit(alert)
                    }
                } catch (e: Exception) {
                    Log.e("STOMP", "Error parseando JSON de alerta", e)
                }
            }, { error ->
                Log.e("STOMP", "Error en suscripción al tópico", error)
            })
        } catch (t: Throwable) {
            Log.e("STOMP", "Falla crítica general en StompService", t)
        }
    }

    fun disconnect() {
        mStompClient?.disconnect()
        mStompClient = null
    }
}
