package com.example.ui.screens.calls

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.PrivoAppContainer
import com.example.domain.model.CallStatus
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoDarkBg
import com.example.ui.theme.PrivoDarkCard
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoIndigo
import com.example.ui.theme.PrivoRoseAccent
import com.example.ui.theme.PrivoVioletPrimary

@Composable
fun ActiveCallScreen(
    container: PrivoAppContainer,
    onCallEnded: () -> Unit
) {
    val callSession by container.callManager.activeCall.collectAsState()
    val pulseScale = remember { Animatable(1.0f) }

    LaunchedEffect(callSession?.status) {
        if (callSession?.status == CallStatus.CONNECTING) {
            pulseScale.animateTo(
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1.0f)
        }
    }

    LaunchedEffect(callSession) {
        if (callSession == null) {
            onCallEnded()
        }
    }

    val session = callSession ?: return

    val formattedDuration = String.format(
        "%02d:%02d",
        session.durationSeconds / 60,
        session.durationSeconds % 60
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrivoDarkBg)
    ) {
        // Main Call Center
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing Avatar Ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale.value)
                    .border(
                        3.dp,
                        Brush.radialGradient(listOf(PrivoVioletPrimary, PrivoCyanAccent)),
                        CircleShape
                    )
                    .background(PrivoDarkCard, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.recipientAvatar,
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = session.recipientName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (session.status) {
                    CallStatus.CONNECTING -> "در حال برقراری تماس زنده LiveKit..."
                    CallStatus.CONNECTED -> "متصل شد • $formattedDuration"
                    CallStatus.DISCONNECTED -> "تماس به پایان رسید"
                    CallStatus.FAILED -> "خطا در اتصال"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (session.status == CallStatus.CONNECTED) PrivoEmeraldSuccess else Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (session.isVideo) "تماس تصویری HD" else "تماس صوتی ابری",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrivoCyanAccent
                )
            }
        }

        // Bottom Call Controls Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic Toggle
                IconButton(
                    onClick = { container.callManager.toggleMute() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (session.isMuted) PrivoRoseAccent else Color.White.copy(alpha = 0.2f))
                        .testTag("call_toggle_mute_button")
                ) {
                    Icon(
                        imageVector = if (session.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "بی‌صدا کردن",
                        tint = Color.White
                    )
                }

                // Camera Toggle (if video)
                if (session.isVideo) {
                    IconButton(
                        onClick = { container.callManager.toggleCamera() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (!session.isCameraOn) PrivoRoseAccent else Color.White.copy(alpha = 0.2f))
                            .testTag("call_toggle_camera_button")
                    ) {
                        Icon(
                            imageVector = if (session.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "دوربین",
                            tint = Color.White
                        )
                    }

                    // Flip Camera
                    IconButton(
                        onClick = { /* Flip camera */ },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("call_flip_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "تغییر دوربین",
                            tint = Color.White
                        )
                    }
                }

                // Speakerphone Toggle
                IconButton(
                    onClick = { container.callManager.toggleSpeaker() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (session.isSpeakerOn) PrivoIndigo else Color.White.copy(alpha = 0.2f))
                        .testTag("call_toggle_speaker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "بلندگو",
                        tint = Color.White
                    )
                }

                // End Call Button
                IconButton(
                    onClick = { container.callManager.endCall() },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrivoRoseAccent)
                        .testTag("call_end_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "پایان تماس",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
