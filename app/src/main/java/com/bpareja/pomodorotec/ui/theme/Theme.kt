package com.bpareja.pomodorotec.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    secondary = Color(0xFFFFE66D),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF3F3F3F),
    onPrimaryContainer = Color.White,
    surfaceVariant = Color(0xFF252525)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB22222),
    secondary = Color(0xFFFFD740),
    background = Color(0xFFFFF0F0),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFB22222),
    onSurface = Color(0xFFB22222),
    primaryContainer = Color(0xFFFFE6E6),
    onPrimaryContainer = Color(0xFFB22222),
    surfaceVariant = Color(0xFFFFE6E6)
)

@Composable
fun PomodoroTecTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Usa la preferencia del sistema por defecto
    content: @Composable () -> Unit
) {
    // Selecciona el esquema de colores basado en el modo oscuro/claro
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Aplica el tema utilizando MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,    // Aplica el esquema de colores seleccionado
        typography = Typography,       // Usa la tipografía definida en Typography.kt
        content = content             // Envuelve el contenido con el tema
    )
}