package com.siscontrol.mobile.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import kotlin.math.sqrt

class EmergencyManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var acceleration = 0f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    
    private var lastShakeTimestamp = 0L
    private val SHAKE_COOLDOWN = 5000L // 5 segundos de espera entre alertas

    private var onShakeDetected: (() -> Unit)? = null

    fun startListening(callback: () -> Unit) {
        onShakeDetected = callback
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI // Cambiado a UI para mejor respuesta sin saturar
        )
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt(x * x + y * y + z * z)
        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta
        
        // Umbral de agitación fuerte y validación de tiempo (Cooldown)
        if (acceleration > 18) { // Subimos un poco el umbral para evitar falsos positivos
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTimestamp > SHAKE_COOLDOWN) {
                lastShakeTimestamp = currentTime
                android.util.Log.d("EMERGENCY", "¡Agitación detectada! Enviando alerta única.")
                
                // Feedback táctil: Vibración de 1 segundo
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(1000)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EMERGENCY", "Error al vibrar", e)
                }
                
                onShakeDetected?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
