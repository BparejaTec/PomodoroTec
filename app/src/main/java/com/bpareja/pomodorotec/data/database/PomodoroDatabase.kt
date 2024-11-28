package com.bpareja.pomodorotec.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bpareja.pomodorotec.data.dao.PomodoroSessionDao
import com.bpareja.pomodorotec.data.model.PomodoroSession
import com.bpareja.pomodorotec.util.Converters

@Database(entities = [PomodoroSession::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class PomodoroDatabase : RoomDatabase() {

    abstract fun pomodoroSessionDao(): PomodoroSessionDao

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
