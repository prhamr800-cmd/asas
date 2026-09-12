package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.AdminMetricsResponse
import com.example.data.repository.AdminRepository
import com.example.ui.theme.PrivoAmberWarning
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoRoseAccent
import com.example.ui.theme.PrivoVioletPrimary
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    adminRepository: AdminRepository,
    onNavigateBack: () -> Unit
) {
    var metrics by remember { mutableStateOf<AdminMetricsResponse?>(null) }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastContent by remember { mutableStateOf("") }
    var isSendingBroadcast by remember { mutableStateOf(false) }
    var broadcastNotice by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        metrics = adminRepository.getMetrics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("admin_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "داشبورد مدیریت کل (پرهام)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "وضعیت زنده سرورها و پیام‌رسان بومی پریوو",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Metrics Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "کل کاربران",
                value = (metrics?.totalUsers ?: 48).toString(),
                icon = Icons.Default.Group,
                color = PrivoVioletPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "کاربران آنلاین",
                value = (metrics?.onlineUsers ?: 12).toString(),
                icon = Icons.Default.SupervisedUserCircle,
                color = PrivoEmeraldSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "پیام‌های مبادله‌شده",
                value = (metrics?.totalMessages ?: 1520).toString(),
                icon = Icons.Default.Message,
                color = PrivoCyanAccent,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "تماس‌های فعال",
                value = (metrics?.activeCalls ?: 2).toString(),
                icon = Icons.Default.PhoneInTalk,
                color = PrivoAmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Broadcast Announcement Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = PrivoAmberWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ارسال اطلاعیه همگانی (Broadcast)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = broadcastTitle,
                    onValueChange = { broadcastTitle = it },
                    label = { Text("عنوان اطلاعیه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = broadcastContent,
                    onValueChange = { broadcastContent = it },
                    label = { Text("متن پیام به تمام کاربران...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_content")
                )

                if (broadcastNotice != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = broadcastNotice!!, color = PrivoEmeraldSuccess, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (broadcastContent.isNotBlank()) {
                            isSendingBroadcast = true
                            scope.launch {
                                adminRepository.sendBroadcast(broadcastTitle.trim(), broadcastContent.trim())
                                isSendingBroadcast = false
                                broadcastNotice = "اطلاعیه به تمام کاربران با موفقیت ارسال گردید."
                                broadcastTitle = ""
                                broadcastContent = ""
                            }
                        }
                    },
                    enabled = broadcastContent.isNotBlank() && !isSendingBroadcast,
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("admin_broadcast_send_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrivoAmberWarning),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSendingBroadcast) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ارسال فوری به کل شبکه", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
