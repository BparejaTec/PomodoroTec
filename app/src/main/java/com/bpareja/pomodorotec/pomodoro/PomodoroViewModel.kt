// PomodoroViewModel.kt
package com.bpareja.pomodorotec.pomodoro

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.CountDownTimer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.R

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _timeLeft = MutableLiveData("25:00")
    val timeLeft: LiveData<String> = _timeLeft

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentPhase = MutableLiveData(Phase.FOCUS)
    val currentPhase: LiveData<Phase> = _currentPhase

    private var countDownTimer: CountDownTimer? = null
    private var timeRemainingInMillis: Long = 25 * 60 * 1000L

    fun startFocusSession() {
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        _timeLeft.value = "25:00"
        showPhaseNotification(
            "¡Comienza tu sesión de concentración! 🎯",
            "Mantén el enfoque durante los próximos 25 minutos.",
            true
        )
        startTimer()
    }

    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        timeRemainingInMillis = 5 * 60 * 1000L
        _timeLeft.value = "05:00"
        showPhaseNotification(
            "¡Tiempo de descanso! 🌟",
            "Toma un descanso de 5 minutos. ¡Te lo has ganado!",
            true
        )
        startTimer()
    }

    fun startTimer() {
        if (_isRunning.value == true) return

        _isRunning.value = true
        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)

                // Actualizar notificación cada minuto
                if (seconds == 0L) {
                    updateTimerNotification(minutes)
                }
            }

            override fun onFinish() {
                _isRunning.value = false
                when (_currentPhase.value) {
                    Phase.FOCUS -> {
                        showPhaseNotification(
                            "¡Excelente trabajo! 🎉",
                            "Has completado tu sesión de concentración. ¡Toma un merecido descanso!",
                            false
                        )
                        startBreakSession()
                    }
                    Phase.BREAK -> {
                        showPhaseNotification(
                            "¡Fin del descanso! 🔄",
                            "¿Listo para otra sesión productiva?",
                            false
                        )
                        startFocusSession()
                    }
                    else -> {}
                }
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        showPhaseNotification(
            "Temporizador en pausa ⏸️",
            "No olvides retomar tu sesión",
            true
        )
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        _timeLeft.value = "25:00"
        showPhaseNotification(
            "Temporizador reiniciado 🔄",
            "¡Listo para comenzar una nueva sesión!",
            true
        )
    }

    private fun updateTimerNotification(minutes: Long) {
        val phaseText = if (_currentPhase.value == Phase.FOCUS) "concentración" else "descanso"
        showPhaseNotification(
            "Pomodoro en progreso ⏱️",
            "Quedan $minutes minutos de $phaseText",
            true
        )
    }

    private fun showPhaseNotification(title: String, message: String, showActions: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Crear intents para las acciones
        val pauseIntent = Intent(context, MainActivity::class.java).apply {
            action = "PAUSE_ACTION"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val resetIntent = Intent(context, MainActivity::class.java).apply {
            action = "RESET_ACTION"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pausePendingIntent = PendingIntent.getActivity(
            context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val resetPendingIntent = PendingIntent.getActivity(
            context, 2, resetIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.pomodoro)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)

        if (showActions) {
            if (_isRunning.value == true) {
                builder.addAction(
                    R.drawable.pomodoro,
                    "Pausar",
                    pausePendingIntent
                )
            }
            builder.addAction(
                R.drawable.pomodoro,
                "Reiniciar",
                resetPendingIntent
            )
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(MainActivity.NOTIFICATION_ID)
    }
}