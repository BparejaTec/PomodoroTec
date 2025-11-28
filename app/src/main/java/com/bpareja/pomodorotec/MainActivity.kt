package com.bpareja.pomodorotec

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel
import androidx.activity.viewModels
import com.bpareja.pomodorotec.pomodoro.PomodoroScreen
import android.net.Uri
import android.media.AudioAttributes



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

        }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri = android.net.Uri.parse("android.resource://${packageName}/${R.raw.pomodoro_alarm}")

            val attributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal Pomodoro",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones del temporizador Pomodoro"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 300, 700)
                setSound(soundUri, attributes)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
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

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        private const val REQUEST_CODE = 1
        const val NOTIFICATION_ID = 1
    }
}