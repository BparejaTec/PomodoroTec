package com.bpareja.pomodorotec.pomodoro

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.CountDownTimer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.R
import com.bpareja.pomodorotec.utils.PreferencesManager

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    init {
        instance = this
    }

    companion object {
        private var instance: PomodoroViewModel? = null
        fun skipBreak() {
            instance?.startFocusSession()  // Saltar el descanso y comenzar sesión de concentración
        }
    }

    private val context = getApplication<Application>().applicationContext
    private val preferencesManager = PreferencesManager(context)

    private val _timeLeft = MutableLiveData("25:00")
    val timeLeft: LiveData<String> = _timeLeft

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentPhase = MutableLiveData(Phase.FOCUS)
    val currentPhase: LiveData<Phase> = _currentPhase

    private val _isSkipBreakButtonVisible = MutableLiveData(false)
    val isSkipBreakButtonVisible: LiveData<Boolean> = _isSkipBreakButtonVisible

    private var countDownTimer: CountDownTimer? = null

    // Las duraciones se obtienen desde PreferencesManager para personalización
    private var sessionDurationInMillis: Long = preferencesManager.getSessionDuration() * 60 * 1000L
    private var breakDurationInMillis: Long = preferencesManager.getBreakDuration() * 60 * 1000L
    private var timeRemainingInMillis: Long = sessionDurationInMillis // Valor inicial basado en el tiempo de sesión

    // Función para iniciar la sesión de concentración
    fun startFocusSession() {
        countDownTimer?.cancel() // Cancela cualquier temporizador en ejecución
        _currentPhase.value = Phase.FOCUS
        sessionDurationInMillis = preferencesManager.getSessionDuration() * 60 * 1000L // Carga personalizada
        timeRemainingInMillis = sessionDurationInMillis
        _timeLeft.value = formatTime(sessionDurationInMillis)
        _isSkipBreakButtonVisible.value = false // Ocultar el botón si estaba visible
        showNotification("Inicio de Concentración", "La sesión de concentración ha comenzado.")
        startTimer() // Inicia el temporizador con el tiempo de enfoque actualizado
    }

    // Función para iniciar la sesión de descanso
    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        breakDurationInMillis = preferencesManager.getBreakDuration() * 60 * 1000L // Carga personalizada
        timeRemainingInMillis = breakDurationInMillis
        _timeLeft.value = formatTime(breakDurationInMillis)
        _isSkipBreakButtonVisible.value = true // Mostrar el botón durante el descanso
        showNotification("Inicio de Descanso", "La sesión de descanso ha comenzado.")
        startTimer()
    }

    // Función para recargar configuraciones desde las preferencias
    fun reloadDurations() {
        sessionDurationInMillis = preferencesManager.getSessionDuration() * 60 * 1000L
        breakDurationInMillis = preferencesManager.getBreakDuration() * 60 * 1000L
        if (_currentPhase.value == Phase.FOCUS) {
            timeRemainingInMillis = sessionDurationInMillis
            _timeLeft.value = formatTime(sessionDurationInMillis)
        } else {
            timeRemainingInMillis = breakDurationInMillis
            _timeLeft.value = formatTime(breakDurationInMillis)
        }
    }

    // Función para guardar configuraciones
    fun updateDurations(sessionMinutes: Int, breakMinutes: Int) {
        preferencesManager.setSessionDuration(sessionMinutes)
        preferencesManager.setBreakDuration(breakMinutes)
        pauseTimer() // Detener el temporizador
        reloadDurations() // Reflejar inmediatamente
    }

    // Función para reiniciar a valores por defecto
    fun resetToDefaultDurations() {
        preferencesManager.setSessionDuration(25)
        preferencesManager.setBreakDuration(5)
        reloadDurations()
    }

    // Inicia o reanuda el temporizador
    fun startTimer() {
        countDownTimer?.cancel() // Cancela cualquier temporizador en ejecución antes de iniciar uno nuevo
        _isRunning.value = true

        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _isRunning.value = false
                when (_currentPhase.value ?: Phase.FOCUS) { // Si es null, se asume FOCUS
                    Phase.FOCUS -> startBreakSession()
                    Phase.BREAK -> startFocusSession()
                }
            }
        }.start()
    }

    // Pausa el temporizador
    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    // Restablece el temporizador
    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _currentPhase.value = Phase.FOCUS
        sessionDurationInMillis = preferencesManager.getSessionDuration() * 60 * 1000L // Carga personalizada
        timeRemainingInMillis = sessionDurationInMillis
        _timeLeft.value = formatTime(sessionDurationInMillis)
        _isSkipBreakButtonVisible.value = false // Ocultar el botón al restablecer
    }

    // Muestra la notificación personalizada
    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT // Reabrir la actividad si ya está en el stack
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Color para la notificación según la fase (rojo para concentración, verde para descanso)
        val notificationColor = if (_currentPhase.value == Phase.FOCUS) 0xFFFF0000.toInt() else 0xFF00FF00.toInt()
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // Sonido predeterminado

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ícono personalizado
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)  // Usar el PendingIntent configurado
            .setAutoCancel(true)
            .setColor(notificationColor) // Color de la notificación
            .setSound(soundUri) // Sonido para la notificación

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

    // Método para formatear el tiempo en formato MM:SS
    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
