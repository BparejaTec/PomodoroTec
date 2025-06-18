package com.bpareja.pomodorotec.wear.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bpareja.pomodorotec.wear.presentation.viewmodel.WearPomodoroViewModel

@Composable
fun WearPomodoroScreen(viewModel: WearPomodoroViewModel) {
    val context = LocalContext.current // Obtener el contexto actual
    val sessionDuration = viewModel.sessionDuration.observeAsState(25).value
    val breakDuration = viewModel.breakDuration.observeAsState(5).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pomodoro Timer", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Concentración: $sessionDuration minutos")
        Text("Descanso: $breakDuration minutos")
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { viewModel.startTimerAction(context) }) { // Pasar el contexto
                Text("Iniciar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.pauseTimerAction(context) }) { // Pasar el contexto
                Text("Pausar")
            }
        }
    }
}
