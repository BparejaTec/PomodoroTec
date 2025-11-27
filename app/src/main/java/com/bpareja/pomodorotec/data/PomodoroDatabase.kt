package com.bpareja.pomodorotec.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PomodoroSession::class], version = 1, exportSchema = false)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroDao(): PomodoroDao
    
    companion object {
        @Volatile
        private var INSTANCE: PomodoroDatabase? = null
        
        fun getDatabase(context: Context): PomodoroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PomodoroDatabase::class.java,
                    "pomodoro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
