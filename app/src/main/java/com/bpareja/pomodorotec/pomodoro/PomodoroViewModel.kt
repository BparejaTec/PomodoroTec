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
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.PomodoroReceiver
import com.bpareja.pomodorotec.R

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    init {
        instance = this
    }
    // Singleton para acceder al ViewModel desde el BroadcastReceiver
    companion object {
        private var instance: PomodoroViewModel? = null
        fun skipBreak() {
            instance?.startFocusSession()  // Saltar el descanso y comenzar sesión de concentración
        }

        // ID para la notificación de agua
        private const val WATER_NOTIFICATION_ID = 2
        // Número de sesiones antes de mostrar la notificación de agua
        private const val SESSIONS_BEFORE_WATER_REMINDER = 2
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

    // Contador para sesiones completadas
    private var completedSessions = 0

    // Variables de control del timer
    private var countDownTimer: CountDownTimer? = null

    private var totalTimeInMillis: Long = 25 * 60 * 1000L // Tiempo total (25 min)

    private var timeRemainingInMillis: Long = 25 * 60 * 1000L // Tiempo inicial para FOCUS

    // Función para iniciar la sesión de concentración
    fun startFocusSession() {
        countDownTimer?.cancel() // Cancela cualquier temporizador en ejecución
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L // Restablece el tiempo de enfoque a 25 minutos
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false // Ocultar el botón si estaba visible
        showNotification("Inicio de Concentración", "La sesión de concentración ha comenzado.")
        startTimer() // Inicia el temporizador con el tiempo de enfoque actualizado
    }

    // Función para iniciar la sesión de descanso
    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        timeRemainingInMillis = 5 * 60 * 1000L // 5 minutos para descanso
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "05:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = true // Mostrar el botón durante el descanso
        showNotification("Inicio de Descanso", "La sesión de descanso ha comenzado.")

        // Incrementar contador de sesiones y verificar si mostrar recordatorio de agua
        completedSessions++
        if (completedSessions >= SESSIONS_BEFORE_WATER_REMINDER) {
            showWaterReminderNotification()
            completedSessions = 0 // Reiniciar contador
        }

        startTimer()
    }

    // Inicia o reanuda el temporizador
    fun startTimer() {
        countDownTimer?.cancel()// Cancela cualquier temporizador en ejecución antes de iniciar uno nuevo
        _isRunning.value = true

        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)

                // Calcular y actualizar el progreso
                val progress = 1f - (millisUntilFinished.toFloat() / totalTimeInMillis.toFloat())
                _progress.value = progress
            }

            override fun onFinish() {
                _isRunning.value = false
                _progress.value = 1f
                when (_currentPhase.value) {
                    Phase.FOCUS -> startBreakSession()
                    Phase.BREAK -> startFocusSession()
                    null -> TODO()
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
        timeRemainingInMillis = 25 * 60 * 1000L // Restablece a 25 minutos
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false // Ocultar el botón al restablecer
    }

    // Muestra la notificación personalizada
    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Mensajes personalizados según la fase
        val customTitle = when (_currentPhase.value) {
            Phase.FOCUS -> "🎯 ¡Tiempo de Concentración!"
            Phase.BREAK -> "☕ ¡Momento de Descanso!"
            else -> title
        }

        // Formateamos el tiempo restante para que sea legible
        val formattedTime = _timeLeft.value?.let { time ->
            if (time != "00:00") time else "Finalizado"
        } ?: "25:00"

        // Mensaje personalizado con emojis y formato
        val customMessage = when (_currentPhase.value) {
            Phase.FOCUS -> "⏰ Tiempo restante: $formattedTime\n" +
                    "💪 ¡Mantén el enfoque en tu tarea!\n" +
                    "🎯 Estás en modo concentración"
            Phase.BREAK -> "⏰ Tiempo restante: $formattedTime\n" +
                    "🌟 ¡Buen trabajo! Toma un respiro\n" +
                    "🧘‍♂ Aprovecha para estirarte"
            else -> message
        }

        // Estilo personalizado para la notificación
        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle(customTitle)
            .bigText(customMessage)
            .setSummaryText("Pomodoro Activo")

        // Colores personalizados según la fase
        val notificationColor = when (_currentPhase.value) {
            Phase.FOCUS -> Color.rgb(178, 34, 34)  // Rojo Pomodoro
            Phase.BREAK -> Color.rgb(46, 139, 87)  // Verde Bosque
            else -> Color.rgb(178, 34, 34)
        }

        // Acción adicional para saltar el descanso
        val skipIntent = Intent(context, PomodoroReceiver::class.java).apply {
            action = "SKIP_BREAK"
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 0, skipIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(customTitle)
            .setContentText(customMessage)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(notificationColor)
            .setColorized(true)  // Colorear toda la notificación
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))  // Patrón de vibración suave
            .setLights(notificationColor, 1000, 1000)    // Luz LED parpadeante

        // Agregar botón de acción solo en fase de descanso
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

    // Nueva función para mostrar la notificación de recordatorio de agua
    private fun showWaterReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle("💧 ¡Hora de Hidratarte!")
            .bigText("Has completado varias sesiones de trabajo. " +
                    "Recuerda mantenerte hidratado para un mejor rendimiento.\n" +
                    "🚰 Toma un vaso de agua fresca")
            .setSummaryText("Recordatorio de Salud")

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💧 ¡Hora de Hidratarte!")
            .setContentText("Toma un momento para beber agua")
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.rgb(33, 150, 243)) // Color azul para agua
            .setColorized(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setLights(Color.BLUE, 1000, 1000)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(WATER_NOTIFICATION_ID, builder.build())
            }
        }
    }
}