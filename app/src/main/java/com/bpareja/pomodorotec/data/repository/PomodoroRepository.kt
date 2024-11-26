package com.bpareja.pomodorotec.data.repository

import com.bpareja.pomodorotec.data.dao.PomodoroSessionDao
import com.bpareja.pomodorotec.data.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

class PomodoroRepository(private val pomodoroSessionDao: PomodoroSessionDao) {
    val allSessions: Flow<List<PomodoroSession>> = pomodoroSessionDao.getAllSessions()

    suspend fun insertSession(session: PomodoroSession) {
        pomodoroSessionDao.insertSession(session)
    }

    suspend fun getFocusSessionsCount(): Int {
        return pomodoroSessionDao.getFocusSessionsCount()
    }

    suspend fun getTotalFocusTime(): Long {
        return pomodoroSessionDao.getTotalFocusTime() ?: 0L
    }
}