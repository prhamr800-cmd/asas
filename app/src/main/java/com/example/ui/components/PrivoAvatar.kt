package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoIndigo
import com.example.ui.theme.PrivoVioletPrimary

@Composable
fun PrivoAvatar(
    emoji: String = "👤",
    colorName: String = "bg-indigo-600",
    size: Dp = 48.dp,
    showOnlineBadge: Boolean = false,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        colorName.contains("amber") -> Color(0xFFD97706)
        colorName.contains("emerald") -> Color(0xFF059669)
        colorName.contains("cyan") -> Color(0xFF0891B2)
        colorName.contains("purple") -> Color(0xFF7C3AED)
        colorName.contains("rose") -> Color(0xFFE11D48)
        colorName.contains("blue") -> Color(0xFF2563EB)
        else -> PrivoIndigo
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(PrivoVioletPrimary.copy(alpha = 0.8f), PrivoIndigo.copy(alpha = 0.5f))
                    ),
                    shape = CircleShape
                )
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.45f).sp
            )
        }

        if (showOnlineBadge) {
            val badgeColor = if (isOnline) PrivoEmeraldSuccess else Color.Gray
            Box(
                modifier = Modifier
                    .size(size * 0.32f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(badgeColor)
            )
        }
    }
}
