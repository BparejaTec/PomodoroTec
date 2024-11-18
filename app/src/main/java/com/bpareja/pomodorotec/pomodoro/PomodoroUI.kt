package com.bpareja.pomodorotec.pomodoro

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun PomodoroScreen(viewModel: PomodoroViewModel = viewModel()) {
    // Estados observables que controlan la UI
    val timeLeft by viewModel.timeLeft.observeAsState("25:00")        // Tiempo restante mostrado
    val isRunning by viewModel.isRunning.observeAsState(false)        // Estado del timer (corriendo/pausado)
    val currentPhase by viewModel.currentPhase.observeAsState(Phase.FOCUS) // Fase actual (FOCUS/BREAK)
    val isSkipBreakButtonVisible by viewModel.isSkipBreakButtonVisible.observeAsState(false) // Visibilidad del botón saltar
    var isDarkTheme by remember { mutableStateOf(false) }             // Control del tema oscuro
    val progress by viewModel.progress.observeAsState(0f)             // Progreso de la barra (0f a 1f)

    // Contenedor principal con tema
    PomodoroTecTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
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
                // Pomodoro image
                Image(
                    painter = painterResource(id = R.drawable.pomodoro),
                    contentDescription = "Imagen de Pomodoro",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )

                // Title y resto del contenido con colores del tema
                Text(
                    text = "Método Pomodoro",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
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
                // Timer grande
                Text(
                    text = timeLeft,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contenedor con padding y sombra para la barra de progreso
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    // Barra de progreso personalizada
                    CustomProgressBar(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                }

                // Porcentaje debajo de la barra
                Text(
                    // Porcentaje de progreso
                    text = "${(progress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones con colores del tema
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.startFocusSession() },
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
            // Botón para saltar el descanso (visible solo en fase BREAK)
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

// Barra de progreso personalizada con efecto de brillo
@Composable
fun CustomProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Contenedor exterior con fondo
    Box(
        modifier = modifier
            .height(24.dp) // Altura aumentada
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // Bordes redondeados
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Barra de progreso interior con efecto shimmer
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .shimmerEffect() // Efecto de brillo
        )
    }
}
// Efecto de brillo animado para la barra de progreso
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    // Animación infinita para el efecto de brillo
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    // Aplica el gradiente animado
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
