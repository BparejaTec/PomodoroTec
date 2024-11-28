package com.bpareja.pomodorotec.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpareja.pomodorotec.R
import com.bpareja.pomodorotec.ui.theme.PomodoroTecTheme

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = viewModel(),
    onNavigateToHistory: () -> Unit
) {
    // Estados observables que controlan la UI
    val timeLeft by viewModel.timeLeft.observeAsState("25:00") // Tiempo restante
    val isRunning by viewModel.isRunning.observeAsState(false) // Estado del temporizador
    val currentPhase by viewModel.currentPhase.observeAsState(Phase.FOCUS) // Fase actual
    val isSkipBreakButtonVisible by viewModel.isSkipBreakButtonVisible.observeAsState(false) // Visibilidad del botón de saltar
    var isDarkTheme by remember { mutableStateOf(false) } // Tema oscuro
    val progress by viewModel.progress.observeAsState(0f) // Progreso del temporizador

    // Tema principal de la pantalla
    PomodoroTecTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Botón de historial en la esquina superior izquierda
            IconButton(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Historial de sesiones",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Switch de tema oscuro en la esquina superior derecha
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modo oscuro",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp),
                    fontSize = 14.sp
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Contenido principal centrado
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Imagen de Pomodoro
                Image(
                    painter = painterResource(id = R.drawable.pomodoro),
                    contentDescription = "Imagen de Pomodoro",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )

                // Título
                Text(
                    text = "Método Pomodoro",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = "Alterna intervalos de 25 minutos de concentración y 5 minutos de descanso para mejorar tu productividad.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Indicador de fase actual
                Text(
                    text = when (currentPhase) {
                        Phase.FOCUS -> "Tiempo de concentración"
                        Phase.BREAK -> "Tiempo de descanso"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tiempo restante
                Text(
                    text = timeLeft,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de progreso
                CustomProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                )

                // Porcentaje de progreso
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones principales
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.startFocusSession() },
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Iniciar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { viewModel.resetTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Reiniciar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Botón para saltar descanso
            if (isSkipBreakButtonVisible) {
                Button(
                    onClick = { PomodoroViewModel.skipBreak() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 70.dp)
                ) {
                    Text("Saltar Descanso", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Barra de progreso personalizada
@Composable
fun CustomProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .shimmerEffect()
        )
    }
}

// Efecto de brillo
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            RepeatMode.Restart
        )
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            ),
            start = Offset(x = translateAnim.value - 1000, y = 0f),
            end = Offset(x = translateAnim.value, y = 0f)
        )
    )
}
