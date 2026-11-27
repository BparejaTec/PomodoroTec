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

    companion object {
        internal var instance: PomodoroViewModel? = null

        fun skipBreak() {
            instance?.startFocusSession()
        }
    }

    private val context = getApplication<Application>().applicationContext

    // Estados observables

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

    // Variables del cronómetro

    private var countDownTimer: CountDownTimer? = null

    private var totalTimeInMillis: Long = 25 * 60 * 1000L
    private var timeRemainingInMillis: Long = 25 * 60 * 1000L

    // Inicio de concentración

    fun startFocusSession() {
        countDownTimer?.cancel()
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false

        showNotification(
            "Inicio de concentración",
            "La sesión de concentración ha comenzado."
        )

        startTimer()
    }

    // Inicio de descanso

    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        timeRemainingInMillis = 5 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "05:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = true

        showNotification(
            "Inicio de descanso",
            "La sesión de descanso ha comenzado."
        )

        startTimer()
    }

    // Funcionamiento del cronómetro

    fun startTimer() {
        countDownTimer?.cancel()
        _isRunning.value = true

        countDownTimer = object : CountDownTimer(
            timeRemainingInMillis, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {

                timeRemainingInMillis = millisUntilFinished

                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60

                _timeLeft.value = String.format(
                    "%02d:%02d", minutes, seconds
                )

                val progress = 1f - (
                        millisUntilFinished.toFloat() /
                                totalTimeInMillis.toFloat()
                        )

                _progress.value = progress

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

    fun updateDurations(
        sessionDuration: Int,
        breakDuration: Int
    ) {
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

    // Pausar cronómetro

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    // Reiniciar cronómetro

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false

        updateWidgetData()
    }

    // Actualización del widget

    private fun updateWidgetData() {

        val prefs = context.getSharedPreferences(
            "pomodoro_prefs",
            Context.MODE_PRIVATE
        )

        prefs.edit().apply {

            putString(
                "phase",
                _currentPhase.value?.let {
                    if (it == Phase.FOCUS)
                        "Concentración"
                    else
                        "Descanso"
                } ?: "Concentración"
            )

            putString(
                "timeLeft",
                _timeLeft.value ?: "25:00"
            )

            putInt(
                "progress",
                ((1f - (
                        timeRemainingInMillis.toFloat() /
                                totalTimeInMillis.toFloat()
                        )) * 100).toInt()
            )

            apply()
        }

        val intent = Intent(
            context,
            com.bpareja.pomodorotec.PomodoroWidgetProvider::class.java
        )

        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(
                ComponentName(
                    context,
                    com.bpareja.pomodorotec.PomodoroWidgetProvider::class.java
                )
            )

        intent.putExtra(
            AppWidgetManager.EXTRA_APPWIDGET_IDS,
            ids
        )

        context.sendBroadcast(intent)
    }

    // NOTIFICACIONES PERSONALIZADAS

    private fun showNotification(title: String, message: String) {

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Nuevos títulos personalizados

        val customTitle = when (_currentPhase.value) {
            Phase.FOCUS -> "🎯 ¡Hora de ser productivo!"
            Phase.BREAK -> "☕ ¡Descanso merecido!"
            else -> title
        }

        val formattedTime = _timeLeft.value?.let {
            if (it != "00:00") it else "Finalizado"
        } ?: "25:00"

        // Nuevos mensajes personalizados

        val customMessage = when (_currentPhase.value) {

            Phase.FOCUS ->
                "Tiempo disponible: $formattedTime\n¡Sigue así, tú puedes!"

            Phase.BREAK ->
                "Tiempo de descanso: $formattedTime\n¡Recarga tus energías!"

            else -> message
        }

        // Imagen según la fase

        val bigImage = BitmapFactory.decodeResource(
            context.resources,
            if (_currentPhase.value == Phase.FOCUS)
                R.drawable.focus_image
            else
                R.drawable.break_image
        )

        val style = NotificationCompat.BigPictureStyle()
            .bigPicture(bigImage)
            .setSummaryText(customMessage)

        // Nuevos colores personalizados

        val notificationColor =
            if (_currentPhase.value == Phase.FOCUS)
                Color.rgb(33, 150, 243)
            else
                Color.rgb(156, 39, 176)

        // Vibración personalizada

        val vibrationPattern =
            if (_currentPhase.value == Phase.FOCUS)
                longArrayOf(0, 150, 100, 150)
            else
                longArrayOf(0, 300, 200, 300)

        // Acción de pausar

        val pauseIntent = Intent(
            context,
            PomodoroReceiver::class.java
        ).apply {
            action = "PAUSE_TIMER"
        }

        val pausePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Acción de reanudar

        val resumeIntent = Intent(
            context,
            PomodoroReceiver::class.java
        ).apply {
            action = "RESUME_TIMER"
        }

        val resumePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            resumeIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Acción de saltar descanso

        val skipIntent = Intent(
            context,
            PomodoroReceiver::class.java
        ).apply {
            action = "SKIP_BREAK"
        }

        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            skipIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Acción de finalizar

        val endIntent = Intent(
            context,
            PomodoroReceiver::class.java
        ).apply {
            action = "END_TIMER"
        }

        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            endIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val progress = (
                (timeRemainingInMillis * 100) /
                        totalTimeInMillis
                ).toInt()

        // Creación de la notificación personalizada

        val builder = NotificationCompat.Builder(
            context,
            MainActivity.CHANNEL_ID
        )
            .setSmallIcon(
                if (_currentPhase.value == Phase.FOCUS)
                    R.drawable.baseline_center_focus_strong_24
                else
                    R.drawable.baseline_free_breakfast_24
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
                    if (_currentPhase.value == Phase.FOCUS)
                        RingtoneManager.TYPE_RINGTONE
                    else
                        RingtoneManager.TYPE_NOTIFICATION
                )
            )

            // Botones personalizados

            .addAction(
                R.drawable.baseline_pause_circle_24,
                "Pausar",
                pausePendingIntent
            )
            .addAction(
                R.drawable.ic_resume,
                "Reanudar",
                resumePendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
                "Finalizar",
                endPendingIntent
            )

        if (_currentPhase.value == Phase.BREAK) {
            builder.addAction(
                R.drawable.ic_skip,
                "Volver a estudiar",
                skipPendingIntent
            )
        }

        // Validación del permiso y envío

        with(NotificationManagerCompat.from(context)) {

            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                notify(
                    MainActivity.NOTIFICATION_ID,
                    builder.build()
                )
            }
        }
    }
}