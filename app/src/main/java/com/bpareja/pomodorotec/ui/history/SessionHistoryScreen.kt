package com.bpareja.pomodorotec.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bpareja.pomodorotec.data.model.PomodoroSession
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(
    viewModel: PomodoroViewModel,
    onNavigateToTimer: () -> Unit
) {
    val sessions by viewModel.allSessions.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Sesiones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToTimer) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions) { session ->
                SessionCard(session = session)
            }
        }
    }
}

@Composable
fun SessionCard(session: PomodoroSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = when(session.type) {
                    "FOCUS" -> "Sesión de Concentración"
                    else -> "Sesión de Descanso"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duración: ${session.duration / 1000 / 60} minutos",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Completada: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(session.completedAt)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (session.wasSkipped) {
                Text(
                    text = "Sesión saltada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}