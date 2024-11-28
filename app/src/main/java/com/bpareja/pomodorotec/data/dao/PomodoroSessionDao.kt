package com.bpareja.pomodorotec.data.dao

import androidx.room.*
import com.bpareja.pomodorotec.data.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Insert
    suspend fun insertSession(session: PomodoroSession)

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'FOCUS'")
    suspend fun getFocusSessionsCount(): Int

    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE type = 'FOCUS'")
    suspend fun getTotalFocusTime(): Long?
}