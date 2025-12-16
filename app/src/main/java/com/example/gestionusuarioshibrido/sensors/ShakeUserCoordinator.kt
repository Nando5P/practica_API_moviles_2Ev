package com.example.gestionusuarioshibrido.sensors

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.gestionusuarioshibrido.data.testUsers
import com.example.gestionusuarioshibrido.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/**
 * Clase responsable de iniciar y detener el SensorShakeDetector
 * y cuando detecta una sacudida ejecuta una sincronización
 */
class ShakeUserCoordinator(
    private val context: Context,
    private val userViewModel: UserViewModel
) {
    private val sensorShakeDetector: SensorShakeDetector

    init {
        // Inicializar el detector y definir el callback
        sensorShakeDetector = SensorShakeDetector(context) {
            handleShakeEvent()
        }
    }

    /**
     * Lógica que se ejecuta al detectar una sacudida.
     */
    private fun handleShakeEvent() {
        Log.d("ShakeCoordinator", "Sacudida detectada -> Sync")

        // Feedback visual rápido para el usuario
        Toast.makeText(context, "¡Sacudida! Sincronizando...", Toast.LENGTH_SHORT).show()

        // Llamada al ViewModel para iniciar la sincronización real
        userViewModel.sync()
    }

    fun startListening() {
        sensorShakeDetector.start()
    }

    fun stopListening() {
        sensorShakeDetector.stop()
    }
}