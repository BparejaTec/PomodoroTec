package com.bpareja.pomodorotec.pomodoro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CountdownAnimation(
    timeLeft: String,
    progress: Float,
    isRunning: Boolean,
    phase: Phase,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(progress, isRunning) {
        if (isRunning) {
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
        } else {
            animatedProgress.snapTo(progress)
        }
    }

    val circleColor = when (phase) {
        Phase.FOCUS -> Color(0xFFB22222)
        Phase.BREAK -> Color(0xFF4CAF50)
    }

    val backgroundColor = if (MaterialTheme.colorScheme.isLight) {
        Color(0xFFE0E0E0)
    } else {
        Color(0xFF424242)
    }

    Box(
        modifier = modifier.size(100.dp), // Reducido de 200dp a 140dp
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) { // Reducido de 200dp a 140dp
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round) // Reducido de 12f a 8f
            )

            drawArc(
                color = circleColor,
                startAngle = -90f,
                sweepAngle = 360f * (1f - animatedProgress.value),
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round) // Reducido de 12f a 8f
            )
        }

        Text(
            text = timeLeft,
            fontSize = 32.sp, // Reducido de 48sp a 32sp
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private val androidx.compose.material3.ColorScheme.isLight: Boolean
    get() = surface.red > 0.5f