package com.bpareja.pomodorotec

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel
import com.bpareja.pomodorotec.pomodoro.PomodoroScreen
import com.bpareja.pomodorotec.components.ChatbotBubble
import com.bpareja.pomodorotec.components.ChatbotPanel
import com.example.pomodorotec.viewmodel.ChatbotViewModel

class MainActivity : ComponentActivity() {

    private val pomodoroViewModel: PomodoroViewModel by viewModels()
    private val chatbotViewModel = ChatbotViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pomodoroViewModel.updateTimerData()

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {

                // ⏳ TU UI PRINCIPAL
                PomodoroScreen(pomodoroViewModel)

                // 🟣 BURBUJA DEL CHATBOT (abajo a la derecha)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                ) {
                    ChatbotBubble(chatbotViewModel)
                }

                // 📩 PANEL DEL CHATBOT
                ChatbotPanel(chatbotViewModel)
            }
        }

        createNotificationChannel()
        requestNotificationPermission()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal Pomodoro"
            val descriptionText = "Notificaciones para el temporizador Pomodoro"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = descriptionText
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

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        private const val REQUEST_CODE = 1
        const val NOTIFICATION_ID = 1
    }
}
