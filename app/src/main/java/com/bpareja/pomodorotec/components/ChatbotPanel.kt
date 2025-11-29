package com.bpareja.pomodorotec.components


import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorotec.viewmodel.ChatbotViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatbotPanel(viewModel: ChatbotViewModel) {

    AnimatedVisibility(
        visible = viewModel.isPanelOpen.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                "🤖 Asistente Inteligente",
                color = Color(0xFF3F51B5),
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = viewModel.userTask.value,
                onValueChange = { viewModel.userTask.value = it },
                label = { Text("¿Qué tarea quieres hacer hoy?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.predictSessions(viewModel.userTask.value) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text("Analizar")
            }

            viewModel.prediction.value?.let { value ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎯 Necesitarás aproximadamente $value sesiones de pomodoro.",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
    }
}
