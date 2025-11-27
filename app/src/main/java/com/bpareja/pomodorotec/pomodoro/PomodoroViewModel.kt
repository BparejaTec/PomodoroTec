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
import androidx.lifecycle.viewModelScope
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.PomodoroReceiver
import com.bpareja.pomodorotec.R
import com.bpareja.pomodorotec.utils.DataSyncManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.bpareja.pomodorotec.data.PomodoroDatabase
import com.bpareja.pomodorotec.data.PomodoroSession
import com.bpareja.pomodorotec.data.SessionType
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    
    // DAO para base de datos
    private val pomodoroDao = PomodoroDatabase.getDatabase(application).pomodoroDao()

    init {
        instance = this
        refreshSessionCount()
        // Guardar timestamp inicial
        val sharedPreferences = context.getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong("LAST_ACTIVITY_TIMESTAMP", System.currentTimeMillis()).apply()
    }
    // Singleton para acceder al ViewModel desde el BroadcastReceiver
    companion object {
        internal var instance: PomodoroViewModel? = null
        fun skipBreak() {
            instance?.startFocusSession()  // Saltar el descanso y comenzar sesión de concentración
        }
    }



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
    
    // Variables para mejorar notificaciones
    private var completedPomodoros: Int = 0
    private var lastProgressPercentage: Int = 0
    private var isDarkMode: Boolean = false

    fun setDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }

    // ----------- FUNCIONES PRINCIPALES ------------

    fun startFocusSession() {
        countDownTimer?.cancel()
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "25:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = false
        lastProgressPercentage = 0
        showNotification("Inicio de Concentración", "La sesión de concentración ha comenzado.", isMilestone = true)
        startTimer()
    }

    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        timeRemainingInMillis = 5 * 60 * 1000L
        totalTimeInMillis = timeRemainingInMillis
        _timeLeft.value = "05:00"
        _progress.value = 0f
        _isSkipBreakButtonVisible.value = true
        lastProgressPercentage = 0
        showNotification("Inicio de Descanso", "La sesión de descanso ha comenzado.", isMilestone = true)
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

                // Actualizar widget siempre
                updateWidgetData()
                
                // Calcular porcentaje actual
                val currentPercentage = ((1f - (millisUntilFinished.toFloat() / totalTimeInMillis.toFloat())) * 100).toInt()
                
                // Actualizar notificación visualmente cada vez que cambia el porcentaje
                if (currentPercentage != lastProgressPercentage) {
                    val isMilestone = currentPercentage % 25 == 0 && currentPercentage != 0 && currentPercentage != 100
                    
                    val message = if (isMilestone) "Has completado el $currentPercentage%" else "Sesión en Progreso"
                    
                    showNotification(
                        title = "Sesión en Progreso", 
                        message = message, 
                        isMilestone = isMilestone
                    )
                    lastProgressPercentage = currentPercentage
                }
            }
            override fun onFinish() {
                _isRunning.value = false
                _progress.value = 1f
                when (_currentPhase.value) {
                    Phase.FOCUS -> {
                        completedPomodoros++
                        checkDailyGoal()
                        
                        // Guardar sesión en base de datos
                        saveSessionToDatabase(
                            type = SessionType.FOCUS,
                            duration = 25,
                            completed = true
                        )
                        
                        startBreakSession()
                    }
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

    // -------------- GAMIFICACIÓN -----------------
    
    private fun checkDailyGoal() {
        if (completedPomodoros == 8) {
            showDailyGoalNotification()
        }
    }
    
    private fun showDailyGoalNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_center_focus_strong_24)
            .setContentTitle("🎉 ¡META DIARIA ALCANZADA!")
            .setContentText("Has completado 8 pomodoros hoy - ¡Eres increíble!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🏆 ¡FELICITACIONES!\n\n" +
                        "✅ 8 Pomodoros completados\n" +
                        "⏱️ 4 horas de concentración profunda\n" +
                        "💪 Productividad nivel EXPERTO\n\n" +
                        "Tu dedicación es inspiradora. ¡Sigue así!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.rgb(255, 193, 7))  // Dorado para celebración
            .setColorized(true)
            .setLights(Color.rgb(255, 193, 7), 1000, 1000)
            .setVibrate(longArrayOf(0, 200, 100, 200, 100, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(2, builder.build())  // Usa ID diferente (2) para no reemplazar la notificación principal
            }
        }
    }
    
    // -------------- ACTUALIZACIÓN DE WIDGET -----------------

    private fun updateWidgetData() {
        // Guarda datos en SharedPreferences
        val prefs = context.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("phase", _currentPhase.value?.let { if (it == Phase.FOCUS) "Concentración" else "Descanso" } ?: "Concentración")
            .putString("timeLeft", _timeLeft.value ?: "25:00")
            .putInt("progress", ((1f - (timeRemainingInMillis.toFloat() / totalTimeInMillis.toFloat())) * 100).toInt())
            .apply()
        // Fuerza actualización de widget
        val intent = Intent(context, com.bpareja.pomodorotec.PomodoroWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, com.bpareja.pomodorotec.PomodoroWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    // ----------------- NOTIFICACIÓN ESTÉTICA MINIMALISTA ------------------------

    private fun showNotification(title: String, message: String, isMilestone: Boolean = false) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Título minimalista y elegante
        val customTitle = when (_currentPhase.value) {
            Phase.FOCUS -> "Concentración Profunda"
            Phase.BREAK -> "Descanso Activo"
            else -> title
        }
        
        // Formato de tiempo limpio
        val formattedTime = _timeLeft.value?.let { 
            if (it != "00:00") it else "Completado" 
        } ?: "25:00"
        
        // Mensaje motivacional MINIMALISTA (sin emojis)
        val motivationalMsg = when (_currentPhase.value) {
            Phase.FOCUS -> listOf(
                "Estás creando algo extraordinario",
                "Tu enfoque es tu superpoder",
                "Momento de brillar con intensidad",
                "El trabajo profundo transforma",
                "Concentración en estado puro"
            ).random()
            Phase.BREAK -> listOf(
                "Recarga tu energía vital",
                "La pausa es parte del éxito",
                "Respira y renuévate",
                "Tu cuerpo merece este momento",
                "Desconexión consciente"
            ).random()
            else -> message
        }
        
        // Calcular porcentaje y barra minimalista
        val percentage = ((1f - (timeRemainingInMillis.toFloat() / totalTimeInMillis.toFloat())) * 100).toInt()
        val progressBar = createMinimalistProgressBar(percentage)
        
        // Calcular estadísticas
        val totalMinutesConcentration = completedPomodoros * 25
        val totalHours = totalMinutesConcentration / 60
        val remainingMinutes = totalMinutesConcentration % 60
        
        // Texto MINIMALISTA con estructura limpia
        val bigTextContent = when (_currentPhase.value) {
            Phase.FOCUS -> buildString {
                append("─────────────────────────\n\n")
                append("$motivationalMsg\n\n")
                append("─────────────────────────\n\n")
                append("TIEMPO\n")
                append("  $formattedTime restantes\n\n")
                append("PROGRESO\n")
                append("  $progressBar $percentage%\n\n")
                append("HOY\n")
                append("  $completedPomodoros sesiones completadas\n")
                append("  ${totalHours}h ${remainingMinutes}m de concentración\n")
                if (completedPomodoros >= 4) {
                    append("  En racha productiva\n")
                }
                append("\n─────────────────────────")
            }
            Phase.BREAK -> buildString {
                append("─────────────────────────\n\n")
                append("$motivationalMsg\n\n")
                append("─────────────────────────\n\n")
                append("DESCANSO\n")
                append("  $formattedTime para renovarte\n\n")
                append("PROGRESO\n")
                append("  $progressBar $percentage%\n\n")
                append("LOGROS\n")
                append("  $completedPomodoros sesiones profundas\n")
                append("  ${totalHours}h ${remainingMinutes}m de trabajo enfocado\n\n")
                append("RECOMENDACIONES\n")
                append("  • Estira suavemente\n")
                append("  • Hidrata tu cuerpo\n")
                append("  • Respira conscientemente\n")
                append("\n─────────────────────────")
            }
            else -> motivationalMsg
        }

        // Ícono grande circular
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            if (_currentPhase.value == Phase.FOCUS) R.drawable.focus_image
            else R.drawable.break_image
        )
        
        // BigTextStyle minimalista
        val style = NotificationCompat.BigTextStyle()
            .bigText(bigTextContent)
            .setBigContentTitle(customTitle)
            .setSummaryText(if (_currentPhase.value == Phase.FOCUS) 
                "Sesión ${completedPomodoros + 1}" 
                else 
                "Momento de descanso")

        // PALETA DE COLORES (Adaptable a Modo Oscuro)
        val notificationColor = if (isDarkMode) {
            if (_currentPhase.value == Phase.FOCUS) 
                Color.rgb(194, 24, 91)    // Pink 700 (Más oscuro y elegante)
            else 
                Color.rgb(69, 90, 100)    // Blue Grey 700
        } else {
            if (_currentPhase.value == Phase.FOCUS) 
                Color.rgb(236, 64, 122)   // Pink 400 (Brillante)
            else 
                Color.rgb(240, 98, 146)   // Pink 300
        }

        // Vibración suave y elegante (SOLO SI ES HITO)
        val vibrationPattern = if (isMilestone) {
            if (_currentPhase.value == Phase.FOCUS)
                longArrayOf(0, 50, 50, 50)       // Vibración muy corta y sutil (Tic-Tic)
            else
                longArrayOf(0, 100, 50, 100)     // Vibración suave relajante
        } else {
            null // Sin vibración para actualizaciones visuales
        }

        // ... (Pending Intents omitted for brevity, they remain the same) ...

        val soundUri = if (isMilestone) {
            // Usamos SIEMPRE TYPE_NOTIFICATION para que sea corto y elegante
            // TYPE_RINGTONE suele ser demasiado largo y molesto para hitos frecuentes
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            null // Sin sonido para actualizaciones visuales
        }

        // ----------- PENDING INTENTS -----------
        
        val pauseIntent = Intent(context, PomodoroReceiver::class.java).apply { 
            action = "PAUSE_TIMER" 
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = Intent(context, PomodoroReceiver::class.java).apply { 
            action = "RESUME_TIMER" 
        }
        val resumePendingIntent = PendingIntent.getBroadcast(
            context, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, PomodoroReceiver::class.java).apply { 
            action = "SKIP_BREAK" 
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 3, skipIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val endIntent = Intent(context, PomodoroReceiver::class.java).apply { 
            action = "END_TIMER" 
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context, 4, endIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val progress = ((timeRemainingInMillis * 100) / totalTimeInMillis).toInt()



        // ----------- CONSTRUCCIÓN MINIMALISTA -----------
        
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(
                if (_currentPhase.value == Phase.FOCUS) 
                    R.drawable.baseline_center_focus_strong_24
                else 
                    R.drawable.baseline_free_breakfast_24
            )
            .setLargeIcon(largeIcon)
            .setContentTitle(customTitle)
            .setContentText("$formattedTime · $percentage% · $completedPomodoros sesiones")
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(pendingIntent)
            .setOngoing(_isRunning.value == true)
            .setAutoCancel(false)
            .setColor(notificationColor)
            .setColorized(true)
            .setLights(notificationColor, 1000, 1000)
            .setProgress(100, progress, false)
            .setSubText(if (_currentPhase.value == Phase.FOCUS) 
                "Concentración" 
                else 
                "Descanso")
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setUsesChronometer(_isRunning.value == true)
            .setOnlyAlertOnce(!isMilestone) // IMPORTANTE: Solo alertar si es hito

        // Configurar sonido y vibración solo si es hito
        if (isMilestone) {
            builder.setVibrate(vibrationPattern)
            builder.setSound(soundUri)
        } else {
            builder.setVibrate(longArrayOf(0)) // Vibración vacía para evitar defaults
            builder.setSound(null)
        }

        // ----------- BOTONES MINIMALISTAS -----------
        
        if (_isRunning.value == true) {
            builder.addAction(
                R.drawable.baseline_pause_circle_24, 
                "Pausar", 
                pausePendingIntent
            )
        } else {
            builder.addAction(
                R.drawable.ic_resume, 
                "Continuar", 
                resumePendingIntent
            )
        }
        
        builder.addAction(
            R.drawable.ic_stop, 
            "Detener", 
            endPendingIntent
        )

        if (_currentPhase.value == Phase.BREAK) {
            builder.addAction(
                R.drawable.baseline_center_focus_strong_24,
                "Volver",
                skipPendingIntent
            )
        }

        // ----------- PUBLICAR -----------
        
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
    
    // Barra de progreso minimalista sin caracteres pesados
    private fun createMinimalistProgressBar(percentage: Int): String {
        val filled = percentage / 5
        val empty = 20 - filled
        return "${"▰".repeat(filled)}${"▱".repeat(empty)}"
    }

    // Helper para guardar sesión
    private fun saveSessionToDatabase(
        type: SessionType,
        duration: Int,
        completed: Boolean
    ) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            // Ajustar día de la semana (Lunes=1 ... Domingo=7 si se desea, o usar Calendar constants)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            
            val session = PomodoroSession(
                timestamp = System.currentTimeMillis(),
                duration = duration,
                type = type,
                completed = completed,
                dayOfWeek = dayOfWeek,
                weekOfYear = weekOfYear
            )
            pomodoroDao.insertSession(session)
            
            // Actualizar contador después de guardar
            refreshSessionCount()
            
            // Guardar timestamp de última actividad
            val sharedPreferences = context.getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putLong("LAST_ACTIVITY_TIMESTAMP", System.currentTimeMillis()).apply()
        }
    }

    private fun refreshSessionCount() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            
            // Obtener inicio de la semana actual (Lunes a las 00:00:00)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            
            // Obtener fin de la semana actual (Domingo a las 23:59:59)
            calendar.add(Calendar.DAY_OF_WEEK, 6) // Ir al domingo
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endTime = calendar.timeInMillis
            
            // Obtener sesiones de la semana
            val sessions = pomodoroDao.getSessionsInRange(startTime, endTime)
            val focusSessions = sessions.filter { it.type == SessionType.FOCUS && it.completed }
            
            completedPomodoros = focusSessions.size
        }
    }
}
