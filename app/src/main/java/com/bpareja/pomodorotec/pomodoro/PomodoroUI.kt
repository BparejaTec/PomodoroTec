package com.bpareja.pomodorotec.pomodoro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
fun PomodoroScreen(viewModel: PomodoroViewModel = viewModel(),  onSettingsClick: () -> Unit) {
    val timeLeft by viewModel.timeLeft.observeAsState("25:00")
    val isRunning by viewModel.isRunning.observeAsState(false)
    val currentPhase by viewModel.currentPhase.observeAsState(Phase.FOCUS)
    val isSkipBreakButtonVisible by viewModel.isSkipBreakButtonVisible.observeAsState(false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
            .padding(16.dp)
    ) {
        // Main content (Pomodoro image, title, time, etc.)
        Column(
            modifier = Modifier
                .align(Alignment.Center) // This ensures the column is always centered
                .padding(bottom = 10.dp), // Padding to ensure space is provided for the button at the bottom
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pomodoro image
            Image(
                painter = painterResource(id = R.drawable.pomodoro),
                contentDescription = "Imagen de Pomodoro",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Title text
            Text(
                text = "Método Pomodoro",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB22222),
                textAlign = TextAlign.Center
            )

            // Description text
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Alterna intervalos de 25 minutos de concentración y 5 minutos de descanso para mejorar tu productividad.",
                fontSize = 16.sp,
                color = Color(0xFFB22222),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Phase label (focus or break)
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

            // Time left display
            Text(
                text = timeLeft,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB22222),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Control buttons (start and reset)
            Row {
                Button(
                    onClick = { viewModel.startFocusSession() },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Iniciar", color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { viewModel.resetTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Reiniciar", color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Botón para abrir configuración
            Button(
                onClick = onSettingsClick, // Llama a la función pasada como parámetro
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Configurar Duraciones", color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Skip break button, positioned absolutely at the bottom of the screen
        if (isSkipBreakButtonVisible) {
            Button(
                onClick = { PomodoroViewModel.skipBreak() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Position the button at the bottom center
                    .padding(bottom = 70.dp) // Reduce the padding to move the button higher
            ) {
                Text("Saltar Descanso", color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

    }
}



