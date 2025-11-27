package com.bpareja.pomodorotec.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.R
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE)
        val lastActivity = sharedPreferences.getLong("LAST_ACTIVITY_TIMESTAMP", 0)
        val currentTime = System.currentTimeMillis()
        
        // 12 horas en milisegundos
        val twelveHoursMillis = TimeUnit.HOURS.toMillis(12)
        
        if (currentTime - lastActivity >= twelveHoursMillis) {
            showReminderNotification()
        }
        
        return Result.success()
    }

    private fun showReminderNotification() {
        // Crear canal si es necesario (aunque MainActivity ya lo crea, es bueno asegurar)
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "¡Te extrañamos! 🍅 Es hora de una sesión productiva.",
            "¿Listo para enfocar tu mente? Tu Pomodoro te espera.",
            "La consistencia es clave. ¡Vuelve a concentrarte!",
            "12 horas sin verte... ¿Hacemos un Pomodoro?",
            "Tu meta está esperando. ¡Vamos a trabajar!"
        )
        
        val randomMessage = messages.random()

        val builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_center_focus_strong_24)
            .setContentTitle("¡Vuelve a la acción!")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
             val notificationManager = NotificationManagerCompat.from(applicationContext)
             notificationManager.notify(1001, builder.build()) // ID diferente al de la sesión
        } catch (e: SecurityException) {
            // Manejar falta de permisos si es necesario
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios"
            val descriptionText = "Recordatorios de inactividad"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
