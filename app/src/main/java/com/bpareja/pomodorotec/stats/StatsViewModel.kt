package com.bpareja.pomodorotec.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bpareja.pomodorotec.data.PomodoroDatabase
import com.bpareja.pomodorotec.data.PomodoroSession
import com.bpareja.pomodorotec.data.SessionType
import kotlinx.coroutines.launch
import java.util.Calendar

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PomodoroDatabase.getDatabase(application).pomodoroDao()
    
    private val _weeklyStats = MutableLiveData<WeeklyStats>()
    val weeklyStats: LiveData<WeeklyStats> = _weeklyStats
    
    /**
     * Carga las estadísticas de la semana actual
     * Calcula el total de sesiones completadas y el tiempo de concentración
     */
    fun loadWeeklyStats() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            
            // Obtener inicio de la semana actual (Lunes a las 00:00:00)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            
            // Obtener fin de la semana actual (Domingo a las 23:59:59)
            calendar.add(Calendar.DAY_OF_WEEK, 6) // Ir al domingo
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endTime = calendar.timeInMillis
            
            // Obtener todas las sesiones de la semana actual
            val sessions = dao.getSessionsInRange(startTime, endTime)
            
            // Calcular estadísticas
            val stats = calculateStats(sessions)
            _weeklyStats.postValue(stats)
        }
    }
    
    /**
     * Calcula las estadísticas semanales
     * Solo cuenta sesiones FOCUS completadas para el reporte
     */
    private fun calculateStats(sessions: List<PomodoroSession>): WeeklyStats {
        // Filtrar solo sesiones de concentración (FOCUS) que fueron completadas
        val focusSessions = sessions.filter { 
            it.type == SessionType.FOCUS && it.completed 
        }
        
        // Calcular total de minutos de concentración
        val totalMinutes = focusSessions.sumOf { it.duration }
        
        // Agrupar sesiones por día de la semana
        val sessionsPerDay = groupByDay(focusSessions)
        
        // Calcular promedio diario (solo días con sesiones)
        val daysWithSessions = sessionsPerDay.keys.size
        val averagePerDay = if (daysWithSessions > 0) {
            focusSessions.size.toDouble() / daysWithSessions
        } else {
            0.0
        }
        
        return WeeklyStats(
            totalSessions = focusSessions.size,
            totalMinutes = totalMinutes,
            sessionsPerDay = sessionsPerDay,
            bestDay = findBestDay(focusSessions),
            averagePerDay = averagePerDay,
            currentStreak = calculateStreak(focusSessions)
        )
    }
    
    /**
     * Agrupa las sesiones por día de la semana
     */
    private fun groupByDay(sessions: List<PomodoroSession>): Map<Int, Int> {
        return sessions.groupingBy { it.dayOfWeek }.eachCount()
    }
    
    /**
     * Encuentra el día con más sesiones completadas
     */
    private fun findBestDay(sessions: List<PomodoroSession>): String {
        if (sessions.isEmpty()) return "-"
        
        val dayMap = groupByDay(sessions)
        val bestDayInt = dayMap.maxByOrNull { it.value }?.key ?: return "-"
        
        return when(bestDayInt) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> "-"
        }
    }
    
    /**
     * Calcula la racha actual de días consecutivos con sesiones
     */
    private fun calculateStreak(sessions: List<PomodoroSession>): Int {
        if (sessions.isEmpty()) return 0
        
        // Obtener días únicos con sesiones, ordenados por timestamp
        val daysWithSessions = sessions
            .map { it.timestamp }
            .distinct()
            .sortedDescending()
        
        // Calcular racha desde hoy hacia atrás
        val calendar = Calendar.getInstance()
        var streak = 0
        var checkDay = 0
        
        while (checkDay < 7) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_YEAR, -checkDay)
            
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.timeInMillis
            
            val hasSession = daysWithSessions.any { it >= dayStart && it < dayEnd }
            
            if (hasSession) {
                streak++
                checkDay++
            } else {
                break
            }
        }
        
        return streak
    }
}

/**
 * Clase de datos que contiene las estadísticas semanales
 */
data class WeeklyStats(
    val totalSessions: Int,              // Total de sesiones FOCUS completadas
    val totalMinutes: Int,               // Total de minutos de concentración
    val sessionsPerDay: Map<Int, Int>,   // Sesiones agrupadas por día
    val bestDay: String,                 // Día con más sesiones
    val averagePerDay: Double,           // Promedio de sesiones por día (solo días con actividad)
    val currentStreak: Int               // Racha de días consecutivos
)
