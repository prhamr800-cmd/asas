package com.example.ui.screens.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.PrivoAvatar
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoRoseAccent
import com.example.ui.theme.PrivoVioletPrimary

data class CallLogItem(
    val id: String,
    val name: String,
    val emoji: String,
    val time: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isMissed: Boolean
)

@Composable
fun CallsListScreen(
    onStartCall: (name: String, emoji: String, isVideo: Boolean) -> Unit
) {
    val callLogs = listOf(
        CallLogItem("1", "پرهام (مدیریت کل)", "👑", "امروز ۱۰:۱۵", isVideo = true, isIncoming = false, isMissed = false),
        CallLogItem("2", "سارا احمدی", "👩‍💼", "دیروز ۱۸:۳۰", isVideo = false, isIncoming = true, isMissed = false),
        CallLogItem("3", "رضا صادقی", "👨‍💻", "۳ روز پیش", isVideo = false, isIncoming = true, isMissed = true),
        CallLogItem("4", "امید عزیزی", "🚀", "هفته گذشته", isVideo = true, isIncoming = false, isMissed = false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "تماس‌ها",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "کیفیت ابری LiveKit",
                style = MaterialTheme.typography.labelSmall,
                color = PrivoEmeraldSuccess
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(callLogs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartCall(log.name, log.emoji, log.isVideo) }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("call_log_item_${log.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrivoAvatar(
                        emoji = log.emoji,
                        colorName = "bg-indigo-600",
                        size = 50.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = log.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (log.isMissed) PrivoRoseAccent else MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (log.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = null,
                                tint = if (log.isMissed) PrivoRoseAccent else PrivoEmeraldSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = log.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { onStartCall(log.name, log.emoji, log.isVideo) }
                    ) {
                        Icon(
                            imageVector = if (log.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "برقراری تماس مجدد",
                            tint = PrivoVioletPrimary
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 76.dp, end = 20.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}
