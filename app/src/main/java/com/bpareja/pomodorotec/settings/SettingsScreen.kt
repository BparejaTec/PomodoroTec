package com.bpareja.pomodorotec.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSaveClick: (Int, Int) -> Unit
) {
    val viewModel: SettingsViewModel = viewModel()
    val sessionDuration = viewModel.sessionDuration.collectAsState().value
    val breakDuration = viewModel.breakDuration.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value

    var isErrorVisible by remember { mutableStateOf(errorMessage.isNotEmpty()) }

    // Visibilidad del mensaje
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            isErrorVisible = true
            delay(2000)
            isErrorVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Configuración",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB22222),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Imagen de Pomodoro
            Image(
                painter = painterResource(id = R.drawable.pomodoro),
                contentDescription = "Imagen de Pomodoro",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 10.dp)
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animación
            AnimatedVisibility(
                visible = isErrorVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Configurar duración de la sesión
            OutlinedTextField(
                value = sessionDuration,
                onValueChange = { viewModel.updateSessionDuration(it) },
                label = { Text("Duración de la sesión", color = Color(0xFFB22222)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFB22222),
                    unfocusedBorderColor = Color(0xFFB22222),
                    focusedLabelColor = Color(0xFFB22222),
                    cursorColor = Color(0xFFB22222)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Configurar duración del descanso
            OutlinedTextField(
                value = breakDuration,
                onValueChange = { viewModel.updateBreakDuration(it) },
                label = { Text("Duración del descanso", color = Color(0xFFB22222)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFB22222),
                    unfocusedBorderColor = Color(0xFFB22222),
                    focusedLabelColor = Color(0xFFB22222),
                    cursorColor = Color(0xFFB22222)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botones guardar y volver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Volver", color = Color(0xFFB22222), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (errorMessage.isEmpty()) {
                            viewModel.saveSettings()
                            onSaveClick(
                                sessionDuration.toIntOrNull() ?: 25,
                                breakDuration.toIntOrNull() ?: 5
                            )
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB22222)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Guardar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}