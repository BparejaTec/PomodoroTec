package com.bpareja.pomodorotec

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.bpareja.pomodorotec.pomodoro.PomodoroScreen
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PomodoroViewModel by viewModels()

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            PomodoroScreen(viewModel)
        }
        handleIntentAction(intent)
    }

    private fun handleIntentAction(intent: Intent?) {
        intent?.let {
            when (it.action) {
                "PAUSE_ACTION" -> {
                    if (viewModel.isRunning.value == true) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                }
                "RESET_ACTION" -> viewModel.resetTimer()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pomodoro Notifications"
            val descriptionText = "Channel for Pomodoro notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Llama a esta función desde fuera para manejar los intents manualmente
    fun updateWithIntent(intent: Intent?) {
        handleIntentAction(intent)
    }
}
