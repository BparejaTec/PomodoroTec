package com.bpareja.pomodorotec.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,              // Milisegundos desde epoch
    val duration: Int,                // Minutos (25 o 5)
    val type: SessionType,            // FOCUS o BREAK
    val completed: Boolean = true,    // Si se completó o fue interrumpida
    val dayOfWeek: Int,              // 1-7 (Lunes-Domingo)
    val weekOfYear: Int              // Número de semana del año
)

enum class SessionType {
    FOCUS, BREAK
}
