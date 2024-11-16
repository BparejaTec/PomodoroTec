package com.bpareja.pomodorotec.pomodoro

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bpareja.pomodorotec.R
import java.util.Locale

@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel = viewModel()) {
    val context = LocalContext.current
    val timeLeft by viewModel.timeLeft.observeAsState("25:00")
    val isRunning by viewModel.isRunning.observeAsState(false)
    val currentPhase by viewModel.currentPhase.observeAsState(Phase.FOCUS)
    val isSkipBreakButtonVisible by viewModel.isSkipBreakButtonVisible.observeAsState(false)

 Agregar-soporte-para-multiples-idiomas
    // Estado para gestionar el idioma
    var languageCode by remember { mutableStateOf("en") }

    // UI Principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagen de cabecera
        Image(
            painter = painterResource(id = R.drawable.pomodoro),
            contentDescription = stringResource(id = R.string.method_title),
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        // Título del método Pomodoro
        Text(
            text = stringResource(id = R.string.method_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Descripción
        Text(
            text = stringResource(id = R.string.description),
            fontSize = 16.sp,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Fase actual
        Text(
            text = when (currentPhase) {
                Phase.FOCUS -> stringResource(id = R.string.focus_time)
                Phase.BREAK -> stringResource(id = R.string.break_time)
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tiempo restante
        Text(
            text = timeLeft,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB22222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones de iniciar y reiniciar
        Row {
            Button(
                onClick = { viewModel.startFocusSession() },
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(stringResource(id = R.string.start), color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))


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
        }

        // Skip break button, positioned absolutely at the bottom of the screen
        if (isSkipBreakButtonVisible) {
          develop
            Button(
                onClick = { PomodoroViewModel.skipBreak() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Position the button at the bottom center
                    .padding(bottom = 70.dp) // Reduce the padding to move the button higher
            ) {
 Agregar-soporte-para-multiples-idiomas
                Text(stringResource(id = R.string.reset), color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para cambiar el idioma
        Button(
            onClick = {
                languageCode = if (languageCode == "en") "es" else "en"
                updateLanguage(context, languageCode)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White) // Color lila
        ) {
            Text(stringResource(id = R.string.change_language), color = Color(0xFFB22222), fontSize = 16.sp)
        }
    }
}


fun updateLanguage(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

                Text("Saltar Descanso", color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

    }
}

develop
