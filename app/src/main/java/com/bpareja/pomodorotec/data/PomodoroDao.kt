package com.bpareja.pomodorotec.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Insert
    suspend fun insertSession(session: PomodoroSession)
    
    @Query("SELECT * FROM pomodoro_sessions WHERE weekOfYear = :week ORDER BY timestamp DESC")
    fun getSessionsByWeek(week: Int): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getSessionsInRange(startTime: Long, endTime: Long): List<PomodoroSession>
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE weekOfYear = :week AND type = 'FOCUS'")
    suspend fun getFocusSessionCountByWeek(week: Int): Int
    
    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE weekOfYear = :week AND type = 'FOCUS'")
    suspend fun getTotalFocusMinutesByWeek(week: Int): Int?
}
