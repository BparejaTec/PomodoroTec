package com.bpareja.pomodorotec.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.bpareja.pomodorotec.ui.theme.PomodoroTecTheme

/**
 * Actividad que muestra el reporte semanal usando Jetpack Compose
 */
class StatsActivity : ComponentActivity() {

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cargar datos al iniciar
        viewModel.loadWeeklyStats()

        // Obtener preferencia de tema
        val isDarkTheme = intent.getBooleanExtra("IS_DARK_THEME", false)

        setContent {
            PomodoroTecTheme(darkTheme = isDarkTheme) {
                WeeklyReportScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
