package com.bpareja.pomodorotec.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpareja.pomodorotec.R

@Composable
fun WeeklyReportScreen(
    viewModel: StatsViewModel,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val stats by viewModel.weeklyStats.observeAsState()

    // Colores dinámicos según el tema
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFDE8E9)
    val primaryTextColor = if (isDarkTheme) Color(0xFFEF9A9A) else Color(0xFFB22222)
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val accentColor = if (isDarkTheme) Color(0xFFFFCDD2) else Color(0xFFE53935)
    val cardTitleColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF5D4037)
    val iconColor = if (isDarkTheme) Color(0xFFEF9A9A) else Color(0xFF5D4037)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = primaryTextColor
                )
            }
            Text(
                text = "Reporte Semanal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balancear el título
        }

        // Card: Sesiones Completadas
        StatsCard(
            title = "SESIONES COMPLETADAS",
            value = stats?.totalSessions?.toString() ?: "0",
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.padding(bottom = 16.dp),
            textColor = accentColor,
            cardBackgroundColor = cardBackgroundColor,
            titleColor = cardTitleColor,
            iconTint = iconColor
        )

        // Card: Tiempo de Concentración
        val hours = (stats?.totalMinutes ?: 0) / 60
        val minutes = (stats?.totalMinutes ?: 0) % 60
        val timeText = "${hours}h ${minutes}m"
        
        StatsCard(
            title = "TIEMPO DE CONCENTRACIÓN",
            value = timeText,
            icon = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder icon if specific clock icon not avail, using generic
            isVectorIcon = false, // We'll use a standard icon or text if needed, but let's try to find a clock icon or use a default
            modifier = Modifier.padding(bottom = 16.dp),
            textColor = accentColor,
            cardBackgroundColor = cardBackgroundColor,
            titleColor = cardTitleColor,
            iconTint = iconColor
        )

        // Row: Promedio y Mejor Día
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallStatsCard(
                title = "PROMEDIO",
                value = String.format("%.1f", stats?.averagePerDay ?: 0.0),
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f),
                textColor = accentColor,
                cardBackgroundColor = cardBackgroundColor,
                titleColor = cardTitleColor,
                iconTint = iconColor
            )
            SmallStatsCard(
                title = "MEJOR DÍA",
                value = stats?.bestDay ?: "-",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                textColor = accentColor,
                cardBackgroundColor = cardBackgroundColor,
                titleColor = cardTitleColor,
                iconTint = iconColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Motivational Text
        Text(
            text = "¡Sigue construyendo tu\nmejor versión!",
            fontSize = 20.sp,
            color = Color(0xFFE57373),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Back Button
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF9A9A), // Rosa suave
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(50.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp))
        ) {
            Text(
                text = "Volver",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: Any, // ImageVector or Painter
    modifier: Modifier = Modifier,
    textColor: Color,
    cardBackgroundColor: Color,
    titleColor: Color,
    iconTint: Color,
    isVectorIcon: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                if (isVectorIcon && icon is ImageVector) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                } 
                // Note: If using painter, handle it here if needed, or simplify to just Vector for now
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun SmallStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    textColor: Color,
    cardBackgroundColor: Color,
    titleColor: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
