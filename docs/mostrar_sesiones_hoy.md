# Widget: Sesiones completadas hoy

## Objetivo
- Mostrar en la pantalla principal la cantidad de sesiones de concentración completadas en el día, como un widget en Jetpack Compose.
- Persistir el conteo por día para mantener el valor entre ejecuciones.

## Arquitectura y puntos de intervención
- Entrada UI: `app/src/main/java/com/bpareja/pomodorotec/MainActivity.kt:28-30`.
- Pantalla principal: `app/src/main/java/com/bpareja/pomodorotec/pomodoro/PomodoroUI.kt:58-253`.
- Lógica de temporizador y fases: `app/src/main/java/com/bpareja/pomodorotec/pomodoro/PomodoroViewModel.kt:90-116`.
- Persistencia actual de widget del sistema: `app/src/main/java/com/bpareja/pomodorotec/PomodoroWidgetProvider.kt:20-39`.

## Estado y persistencia
- Nuevo estado observable en `PomodoroViewModel`:
  - `todayCompletedSessions: LiveData<Int>` con respaldo `MutableLiveData`.
- Persistencia por día en `SharedPreferences` (`sessions_prefs`) usando clave `sessions_<yyyy-MM-dd>`.

## Cambios en ViewModel
Archivo: `app/src/main/java/com/bpareja/pomodorotec/pomodoro/PomodoroViewModel.kt`

1) Añadir estado:
```kotlin
private val _todayCompletedSessions = MutableLiveData(0)
val todayCompletedSessions: LiveData<Int> = _todayCompletedSessions
```

2) Utilidades de persistencia:
```kotlin
private fun todayKey(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return "sessions_" + sdf.format(java.util.Date())
}

fun loadTodayCompletedSessions() {
    val prefs = context.getSharedPreferences("sessions_prefs", Context.MODE_PRIVATE)
    _todayCompletedSessions.value = prefs.getInt(todayKey(), 0)
}

private fun incrementTodayCompletedSessions() {
    val prefs = context.getSharedPreferences("sessions_prefs", Context.MODE_PRIVATE)
    val key = todayKey()
    val current = prefs.getInt(key, 0) + 1
    prefs.edit().putInt(key, current).apply()
    _todayCompletedSessions.value = current
}
```

3) Incrementar al terminar una sesión de concentración:
Referencia: `app/src/main/java/com/bpareja/pomodorotec/pomodoro/PomodoroViewModel.kt:106-114`
```kotlin
override fun onFinish() {
    _isRunning.value = false
    _progress.value = 1f
    when (_currentPhase.value) {
        Phase.FOCUS -> {
            incrementTodayCompletedSessions()
            startBreakSession()
        }
        Phase.BREAK -> startFocusSession()
        null -> {}
    }
}
```

4) Opcional: incluir el conteo en los datos del AppWidget del sistema:
Referencia: `app/src/main/java/com/bpareja/pomodorotec/pomodoro/PomodoroViewModel.kt:155-171`
```kotlin
val prefs = context.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)
prefs.edit().apply {
    putString("phase", _currentPhase.value?.let { if (it == Phase.FOCUS) "Concentración" else "Descanso" } ?: "Concentración")
    putString("timeLeft", _timeLeft.value ?: "25:00")
    putInt("progress", ((1f - (timeRemainingInMillis.toFloat() / totalTimeInMillis.toFloat())) * 100).toInt())
    putInt("todaySessions", _todayCompletedSessions.value ?: 0)
    apply()
}
```

## Cambios en la UI (Compose)
Archivo: `app/src/main/java/com/bpareja/pomodorotec/pomodoro/PomodoroUI.kt`

1) Observar el nuevo estado:
```kotlin
val todaySessions by viewModel.todayCompletedSessions.observeAsState(0)
```
Colocar junto a los demás `observeAsState` del `PomodoroScreen`.

2) Widget visual con el conteo diario (recomendado debajo del título):
Referencia aproximada de inserción: después de `Text("Método Pomodoro", ...)`.
```kotlin
Spacer(modifier = Modifier.height(8.dp))
Text(
    text = "Sesiones completadas hoy: $todaySessions",
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onBackground,
    textAlign = TextAlign.Center
)
```

## Inicialización en MainActivity
Archivo: `app/src/main/java/com/bpareja/pomodorotec/MainActivity.kt`

- Cargar el conteo al iniciar para que la UI muestre el valor correcto:
Referencia: `app/src/main/java/com/bpareja/pomodorotec/MainActivity.kt:23-30`
```kotlin
viewModel.loadTodayCompletedSessions()
```
Ubicar antes de `setContent { PomodoroScreen(viewModel) }`.

## Opcional AppWidget del sistema
Archivo: `app/src/main/java/com/bpareja/pomodorotec/PomodoroWidgetProvider.kt`

- Para mostrar también el conteo en el widget del sistema, añadir lectura y un `TextView` en `widget_pomodoro.xml`:
```kotlin
val today = prefs.getInt("todaySessions", 0)
views.setTextViewText(R.id.widget_today_sessions, "Hoy: $today")
```

## Consideraciones
- El incremento sucede al finalizar una fase `FOCUS`, que representa una sesión de concentración completada.
- Reiniciar el temporizador no afecta el conteo diario.
- La clave diaria usa la fecha local del dispositivo y se resetea naturalmente al cambiar de día.

## Pruebas y verificación
- Iniciar una sesión de concentración y permitir que finalice: el contador debe aumentar en 1 y mostrarse en la UI.
- Cerrar y abrir la app: el valor debe persistir.
- Cambiar de día (o modificar la fecha del dispositivo para pruebas): el contador debe iniciar en 0 para el nuevo día.

## Entregables
- Código en `PomodoroViewModel` para estado y persistencia.
- Actualización de `PomodoroUI` con el widget visual.
- Inicialización en `MainActivity`.
