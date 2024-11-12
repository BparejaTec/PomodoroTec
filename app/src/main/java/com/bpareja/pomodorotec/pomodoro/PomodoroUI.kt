package com.bpareja.pomodorotec.pomodoro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpareja.pomodorotec.R

@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel = viewModel()) {
    val timeLeft by viewModel.timeLeft.observeAsState("25:00")
    val isRunning by viewModel.isRunning.observeAsState(false)
    val currentPhase by viewModel.currentPhase.observeAsState(Phase.FOCUS)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.pomodoro),
            contentDescription = "Imagen de Pomodoro",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Método Pomodoro",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Alterna intervalos de 25 minutos de concentración y 5 minutos de descanso para mejorar tu productividad.",
            fontSize = 16.sp,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when (currentPhase) {
                Phase.FOCUS -> "Tiempo de concentración"
                Phase.BREAK -> "Tiempo de descanso"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = timeLeft,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(
                onClick = { viewModel.startFocusSession() },
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Iniciar", color = Color(0xFFB22222), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    if (isRunning) "Pausar" else "Reanudar",
                    color = Color(0xFFB22222),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { viewModel.resetTimer() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Reiniciar", color = Color(0xFFB22222), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
