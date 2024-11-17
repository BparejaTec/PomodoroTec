package com.bpareja.pomodorotec.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bpareja.pomodorotec.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application.applicationContext)

    private val _sessionDuration = MutableStateFlow(preferencesManager.getSessionDuration().toString())
    val sessionDuration: StateFlow<String> = _sessionDuration

    private val _breakDuration = MutableStateFlow(preferencesManager.getBreakDuration().toString())
    val breakDuration: StateFlow<String> = _breakDuration

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    // Actualizar duración de la sesión con validación
    fun updateSessionDuration(value: String) {
        if (value.isEmpty() || isValidDuration(value)) {
            _sessionDuration.value = value
            _errorMessage.value = ""
        } else {
            _errorMessage.value = "Duración debe estar entre 1 y 180 minutos"
        }
    }

    // Actualizar duración del descanso con validación
    fun updateBreakDuration(value: String) {
        if (value.isEmpty() || isValidDuration(value)) {
            _breakDuration.value = value
            _errorMessage.value = ""
        } else {
            _errorMessage.value = "Duración debe estar entre 1 y 180 minutos"
        }
    }

    // Guarda configuraciones
    fun saveSettings() {
        preferencesManager.setSessionDuration(_sessionDuration.value.toIntOrNull() ?: 25)
        preferencesManager.setBreakDuration(_breakDuration.value.toIntOrNull() ?: 5)
    }

    // Validación de entrada
    private fun isValidDuration(duration: String): Boolean {
        val value = duration.toIntOrNull()
        return value != null && value in 1..180
    }
}
