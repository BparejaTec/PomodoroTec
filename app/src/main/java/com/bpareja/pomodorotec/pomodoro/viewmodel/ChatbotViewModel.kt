package com.example.pomodorotec.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt

class ChatbotViewModel : ViewModel() {

    var isBubbleOpen = mutableStateOf(true)
    var isPanelOpen = mutableStateOf(false)
    var userTask = mutableStateOf("")
    var prediction = mutableStateOf<Int?>(null)

    // Modelo “inteligente” SUPER SIMPLE
    fun predictSessions(task: String) {
        userTask.value = task

        val words = task.split(" ").size

        // Detectar dificultad según palabras clave
        val difficulty = when {
            task.contains("proyecto", ignoreCase = true) -> 4.0
            task.contains("aplicación", ignoreCase = true) -> 3.5
            task.contains("investigar", ignoreCase = true) -> 3.0
            task.contains("tarea", ignoreCase = true) -> 2.0
            task.contains("resumen", ignoreCase = true) -> 1.5
            else -> 1.2
        }

        // Cálculo más realista
        val estimated = (words * 0.25 * difficulty)
            .coerceIn(1.0, 20.0) // sesiones entre 1 y 20

        prediction.value = estimated.toInt()

        isPanelOpen.value = true
    }


    fun toggleBubble() {
        isPanelOpen.value = false
        isBubbleOpen.value = !isBubbleOpen.value
    }

}

