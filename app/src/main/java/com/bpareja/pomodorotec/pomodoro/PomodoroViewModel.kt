package com.bpareja.pomodorotec.pomodoro

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.CountDownTimer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.PomodoroReceiver
import com.bpareja.pomodorotec.R
import com.bpareja.pomodorotec.utils.DataSyncManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    init {
        instance = this
    }
    // Singleton para acceder al ViewModel desde el BroadcastReceiver
    companion object {
        internal var instance: PomodoroViewModel? = null
        fun skipBreak() {
            instance?.startFocusSession()  // Saltar el descanso y comenzar sesión de concentración
        }
    }

    private val context = getApplication<Application>().applicationContext

    // Estados observables (LiveData)
    private val _timeLeft = MutableLiveData("25:00") // Tiempo mostrado en UI
    val timeLeft: LiveData<String> = _timeLeft

    private val _isRunning = MutableLiveData(false) // Estado del timer
    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentPhase = MutableLiveData(Phase.FOCUS)// Fase actual
    val currentPhase: LiveData<Phase> = _currentPhase

    private val _isSkipBreakButtonVisible = MutableLiveData(false)// Visibilidad botón saltar
    val isSkipBreakButtonVisible: LiveData<Boolean> = _isSkipBreakButtonVisible

    private val _progress = MutableLiveData(0f) // Progreso (0-1)
    val progress: LiveData<Float> = _progress

    // Variables de control del timer
    private var countDownTimer: CountDownTimer? = null

    private var totalTimeInMillis: Long = 25 * 60 * 1000L // Tiempo total (25 min)
    private var timeRemainingInMillis: Long = 25 * 60 * 1000L // Tiempo inicial para FOCUS

    // ----------- FUNCIONES PRINCIPALES ------------

    fun startFocusSession() {
        countDownTimer?.cancel()
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false
        showNotification("Inicio de Concentración", "La sesión de concentración ha comenzado.")
        startTimer()
    }

    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        timeRemainingInMillis = 5 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "05:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = true
        showNotification("Inicio de Descanso", "La sesión de descanso ha comenzado.")
        startTimer()
    }

    fun startTimer() {
        countDownTimer?.cancel()
        _isRunning.value = true

        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)
                val progress = 1f - (millisUntilFinished.toFloat() / totalTimeInMillis.toFloat())
                _progress.value = progress

                // ----------- GUARDAR DATOS PARA EL WIDGET -------------
                updateWidgetData()
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

    fun updateDurations(sessionDuration: Int, breakDuration: Int) {
        DataSyncManager.sendPomodoroData(
            context = getApplication(),
            sessionDuration = sessionDuration,
            breakDuration = breakDuration
        )
    }

    fun updateTimerData() {
        DataSyncManager.sendPomodoroData(
            context = getApplication(),
            sessionDuration = 25,
            breakDuration = 5
        )
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        // Actualizar notificación si quieres aquí
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false
        // Actualizar widget aquí también si quieres
        updateWidgetData()
    }

    // -------------- ACTUALIZACIÓN DE WIDGET -----------------

    private fun updateWidgetData() {
        // Guarda datos en SharedPreferences
        val prefs = context.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("phase", _currentPhase.value?.let { if (it == Phase.FOCUS) "Concentración" else "Descanso" } ?: "Concentración")
            putString("timeLeft", _timeLeft.value ?: "25:00")
            putInt("progress", ((1f - (timeRemainingInMillis.toFloat() / totalTimeInMillis.toFloat())) * 100).toInt())
            apply()
        }
        // Fuerza actualización de widget
        val intent = Intent(context, com.bpareja.pomodorotec.PomodoroWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, com.bpareja.pomodorotec.PomodoroWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    // ----------------- NOTIFICACIÓN AVANZADA ------------------------

    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val customTitle = when (_currentPhase.value) {
            Phase.FOCUS -> "🎯 ¡Tiempo de Concentración!"
            Phase.BREAK -> "☕ ¡Momento de Descanso!"
            else -> title
        }
        val formattedTime = _timeLeft.value?.let { if (it != "00:00") it else "Finalizado" } ?: "25:00"
        val customMessage = when (_currentPhase.value) {
            Phase.FOCUS -> "⏰ Restan $formattedTime\n💪 ¡Mantén el enfoque!"
            Phase.BREAK -> "⏰ Restan $formattedTime\n🧘‍♂️ ¡Relájate unos minutos!"
            else -> message
        }
        val bigImage = BitmapFactory.decodeResource(
            context.resources,
            if (_currentPhase.value == Phase.FOCUS) R.drawable.focus_image
            else R.drawable.break_image
        )
        val style = NotificationCompat.BigPictureStyle().bigPicture(bigImage)

        val notificationColor = if (_currentPhase.value == Phase.FOCUS) Color.rgb(178, 34, 34) else Color.rgb(46, 139, 87)

        val vibrationPattern = if (_currentPhase.value == Phase.FOCUS)
            longArrayOf(0, 100, 100, 100)
        else
            longArrayOf(0, 500, 500)

        // Intents para acciones
        val pauseIntent = Intent(context, PomodoroReceiver::class.java).apply { action = "PAUSE_TIMER" }
        val pausePendingIntent = PendingIntent.getBroadcast(
            context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = Intent(context, PomodoroReceiver::class.java).apply { action = "RESUME_TIMER" }
        val resumePendingIntent = PendingIntent.getBroadcast(
            context, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, PomodoroReceiver::class.java).apply { action = "SKIP_BREAK" }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 3, skipIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val endIntent = Intent(context, PomodoroReceiver::class.java).apply { action = "END_TIMER" }
        val endPendingIntent = PendingIntent.getBroadcast(
            context, 4, endIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val progress = ((timeRemainingInMillis * 100) / totalTimeInMillis).toInt()

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(
                if (_currentPhase.value == Phase.FOCUS) R.drawable.baseline_center_focus_strong_24
                else R.drawable.baseline_free_breakfast_24
            )
            .setContentTitle(customTitle)
            .setContentText(customMessage)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(notificationColor)
            .setColorized(true)
            .setLights(notificationColor, 1000, 1000)
            .setVibrate(vibrationPattern)
            .setProgress(100, progress, false)
            .setSound(
                RingtoneManager.getDefaultUri(
                    if (_currentPhase.value == Phase.FOCUS) RingtoneManager.TYPE_RINGTONE
                    else RingtoneManager.TYPE_NOTIFICATION
                )
            )
            // Botones
            .addAction(R.drawable.baseline_pause_circle_24, "Pausar", pausePendingIntent)
            .addAction(R.drawable.ic_resume, "Reanudar", resumePendingIntent)
            .addAction(R.drawable.ic_stop, "Terminar", endPendingIntent)

        if (_currentPhase.value == Phase.BREAK) {
            builder.addAction(
                R.drawable.ic_skip,
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
