package com.bpareja.pomodorotec

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel
import androidx.activity.viewModels
import com.bpareja.pomodorotec.pomodoro.PomodoroScreen


class MainActivity : ComponentActivity() {

    private val viewModel: PomodoroViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.updateTimerData()

        setContent {
            PomodoroScreen(viewModel)
        }
        // Crear el canal de notificaciones
        createNotificationChannel()
        // Solicitar permiso para notificaciones en Android 13+
        requestNotificationPermission()
        
        // Programar recordatorio de inactividad
        scheduleInactivityReminder()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "🎯 Temporizador Pomodoro"
            val descriptionText = "Notificaciones elegantes y motivacionales para tus sesiones de concentración y descanso"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)  // Habilitar luz LED
                lightColor = Color.rgb(255, 107, 107)  // Color coral por defecto
                enableVibration(true)  // Habilitar vibración
                setShowBadge(true)  // Mostrar badge en el ícono de la app
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC  // Visible en pantalla bloqueada
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE
                )
            }
        }
    }

    private fun scheduleInactivityReminder() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.bpareja.pomodorotec.workers.ReminderWorker>(
            12, java.util.concurrent.TimeUnit.HOURS
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "InactivityReminder",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        const val CHANNEL_ID = "pomodoro_channel_v2"
        private const val REQUEST_CODE = 1
        const val NOTIFICATION_ID = 1
    }
}