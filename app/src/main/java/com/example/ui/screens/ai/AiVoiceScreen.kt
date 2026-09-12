package com.example.ui.screens.ai

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoDarkBg
import com.example.ui.theme.PrivoRoseAccent
import com.example.ui.theme.PrivoVioletPrimary

@Composable
fun AiVoiceScreen(
    onNavigateBack: () -> Unit
) {
    val orbScale = remember { Animatable(1.0f) }
    var isMuted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orbScale.animateTo(
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrivoDarkBg)
    ) {
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .testTag("ai_voice_back_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
        }

        // Center Voice Orb
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(orbScale.value)
                    .border(
                        4.dp,
                        Brush.sweepGradient(listOf(PrivoVioletPrimary, PrivoCyanAccent, PrivoVioletPrimary)),
                        CircleShape
                    )
                    .background(
                        Brush.radialGradient(listOf(PrivoCyanAccent.copy(alpha = 0.5f), PrivoVioletPrimary.copy(alpha = 0.8f))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙️", fontSize = 54.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "گفتگوی زنده با پرهام AI",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isMuted) "میکروفون بسته است" else "در حال شنیدن صدای شما...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMuted) PrivoRoseAccent else PrivoCyanAccent
            )
        }

        // Controls at Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) PrivoRoseAccent else Color.White.copy(alpha = 0.2f))
                    .testTag("ai_voice_mute_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "میکروفون",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrivoRoseAccent)
                    .testTag("ai_voice_end_button")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "پایان جلسه", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}
