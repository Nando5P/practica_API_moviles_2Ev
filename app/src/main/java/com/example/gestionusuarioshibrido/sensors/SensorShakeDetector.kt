package com.example.gestionusuarioshibrido.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detecta una sacudida del dispositivo usando el acelerómetro.
 *
 * @property context Contexto necesario para acceder al sensor.
 * @property onShake Acción que se ejecutará cuando se detecte una sacudida.
 */
class SensorShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Último tiempo registrado para evitar múltiples eventos seguidos
    private var lastShakeTime = 0L

    // Umbral de fuerza G (2.7 veces la gravedad)
    private val SHAKE_THRESHOLD_GRAVITY = 2.7F
    // Tiempo de espera entre sacudidas (ms)
    private val SHAKE_SLOP_TIME_MS = 500

    /**
     * Registra el listener del acelerómetro.
     */
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Detiene el listener del acelerómetro.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Maneja los eventos generados por el acelerómetro.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Normalizar dividiendo por la gravedad para obtener fuerza G
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Calcular fuerza total
            val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

            // Validar umbral y tiempo de espera
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()
                // Evitar rebotes (múltiples llamadas en la misma sacudida)
                if (lastShakeTime + SHAKE_SLOP_TIME_MS < now) {
                    lastShakeTime = now
                    onShake()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar nada aquí
    }
}