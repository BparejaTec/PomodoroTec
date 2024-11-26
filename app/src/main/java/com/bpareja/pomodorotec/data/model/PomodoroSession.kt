package com.bpareja.pomodorotec.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "FOCUS" o "BREAK"
    val duration: Long, // Duración en milisegundos
    val completedAt: Date,
    val wasSkipped: Boolean = false // Para sesiones de descanso saltadas
)