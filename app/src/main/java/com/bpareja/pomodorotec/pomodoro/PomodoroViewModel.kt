package com.bpareja.pomodorotec.pomodoro

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.CountDownTimer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.PomodoroReceiver
import com.bpareja.pomodorotec.R
import com.bpareja.pomodorotec.data.database.PomodoroDatabase
import com.bpareja.pomodorotec.data.model.PomodoroSession
import com.bpareja.pomodorotec.data.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

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
            instance?.startFocusSession()
        }
    }

    private val context = getApplication<Application>().applicationContext

    // LiveData para la UI
    private val _timeLeft = MutableLiveData("25:00")
    val timeLeft: LiveData<String> = _timeLeft

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentPhase = MutableLiveData(Phase.FOCUS)
    val currentPhase: LiveData<Phase> = _currentPhase

    private val _isSkipBreakButtonVisible = MutableLiveData(false)
    val isSkipBreakButtonVisible: LiveData<Boolean> = _isSkipBreakButtonVisible

    private val _progress = MutableLiveData(0f)
    val progress: LiveData<Float> = _progress

    // Timer variables
    private var countDownTimer: CountDownTimer? = null
    private var totalTimeInMillis: Long = 25 * 60 * 1000L
    private var timeRemainingInMillis: Long = 2 * 60 * 1000L

    // Base de datos y repositorio
    private val repository: PomodoroRepository
    val allSessions: Flow<List<PomodoroSession>>

    init {
        val database = PomodoroDatabase.getDatabase(application)
        val dao = database.pomodoroSessionDao()
        repository = PomodoroRepository(dao)
        allSessions = repository.allSessions
    }

    // Guardar sesión en la base de datos
    private fun saveSession(type: String, duration: Long, wasSkipped: Boolean) {
        viewModelScope.launch {
            val session = PomodoroSession(
                type = type,
                duration = duration,
                completedAt = Date(),
                wasSkipped = wasSkipped
            )
            repository.insertSession(session)
        }
    }

    // Iniciar sesión de concentración
    fun startFocusSession() {
        saveSession("BREAK", totalTimeInMillis, false)
        countDownTimer?.cancel()
        _currentPhase.value = Phase.FOCUS
        resetFocusTimer()
        showNotification("Inicio de Concentración", "La sesión de concentración ha comenzado.")
    }

    // Iniciar sesión de descanso
    private fun startBreakSession() {
        saveSession("FOCUS", totalTimeInMillis, false)
        _currentPhase.value = Phase.BREAK
        resetBreakTimer()
        showNotification("Inicio de Descanso", "La sesión de descanso ha comenzado.")
    }

    // Reinicia temporizador para concentración
    private fun resetFocusTimer() {
        timeRemainingInMillis = 2 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false
        startTimer()
    }

    // Reinicia temporizador para descanso
    private fun resetBreakTimer() {
        timeRemainingInMillis = 1 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "05:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = true
        startTimer()
    }

    // Inicia el temporizador
    private fun startTimer() {
        countDownTimer?.cancel()
        _isRunning.value = true
        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)
                _progress.value = 1f - (millisUntilFinished.toFloat() / totalTimeInMillis.toFloat())
            }

            override fun onFinish() {
                _isRunning.value = false
                _progress.value = 1f
                when (_currentPhase.value) {
                    Phase.FOCUS -> startBreakSession()
                    Phase.BREAK -> startFocusSession()
                    null -> {}
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
        resetFocusTimer()
    }

    // Mostrar notificación
    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, PomodoroReceiver::class.java).apply {
            action = "SKIP_BREAK"
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 0, skipIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (_currentPhase.value == Phase.BREAK) {
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Saltar Descanso",
                skipPendingIntent
            )
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(MainActivity.NOTIFICATION_ID, builder.build())
            }
        }
    }
}
