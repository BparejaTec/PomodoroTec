package com.bpareja.pomodorotec.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pomodorotec.viewmodel.ChatbotViewModel
import com.bpareja.pomodorotec.R


@Composable
fun ChatbotBubble(viewModel: ChatbotViewModel) {

    AnimatedVisibility(
        visible = viewModel.isBubbleOpen.value,
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFF3F51B5))
                .clickable { viewModel.isPanelOpen.value = true }
                .border(3.dp, Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chatbot),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
            )
        }
    }
}
