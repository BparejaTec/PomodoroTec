package com.bpareja.pomodorotec.utils

import android.content.Context

class PreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("PomodoroPreferences", Context.MODE_PRIVATE)

    // Guardar duración de la sesión
    fun setSessionDuration(durationInMinutes: Int) {
        sharedPreferences.edit().putInt("SESSION_DURATION", durationInMinutes).apply()
    }

    // Obtener duración de la sesión
    fun getSessionDuration(): Int {
        return sharedPreferences.getInt("SESSION_DURATION", 25) // Valor por defecto
    }

    // Guardar duración del descanso
    fun setBreakDuration(durationInMinutes: Int) {
        sharedPreferences.edit().putInt("BREAK_DURATION", durationInMinutes).apply()
    }

    // Obtener duración del descanso
    fun getBreakDuration(): Int {
        return sharedPreferences.getInt("BREAK_DURATION", 5) // Valor por defecto
    }
}
