package com.bpareja.pomodorotec.wear.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpareja.pomodorotec.wear.presentation.viewmodel.WearPomodoroViewModel

@Composable
fun WearPomodoroScreen(viewModel: WearPomodoroViewModel) {
    val context = LocalContext.current
    val timeLeft = viewModel.timeLeft.observeAsState("25:00").value
    val phase = viewModel.phase.observeAsState("Concentración").value
    val isRunning = viewModel.isRunning.observeAsState(false).value
    val progress = viewModel.progress.observeAsState(0f).value

    val isBreak = phase == "Descanso"
    val accentColor = if (isBreak) Color(0xFF2E8B57) else Color(0xFFD32F2F)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            color = accentColor,
            trackColor = Color(0xFF2C2C2C),
            strokeWidth = 6.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PomodoroTec",
                color = Color(0xFFB0B0B0),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = phase.uppercase(),
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeLeft,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isRunning) viewModel.pauseTimerAction(context)
                        else viewModel.startTimerAction(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isRunning) "Pausar" else "Iniciar",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.resetTimerAction(context) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Reset",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            if (isBreak) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { viewModel.skipBreakAction(context) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "Saltar",
                        color = Color(0xFF90CAF9),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}