package com.example.ui.screens.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.PrivoAppContainer
import com.example.domain.model.AiSummaryResult
import com.example.domain.model.Message
import com.example.ui.components.PrivoAvatar
import com.example.ui.theme.PrivoBubbleIncomingDark
import com.example.ui.theme.PrivoBubbleOutgoing
import com.example.ui.theme.PrivoBubbleOutgoingGradient
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoRoseAccent
import com.example.ui.theme.PrivoVioletPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    chatId: String,
    chatName: String,
    chatEmoji: String,
    isGroup: Boolean,
    container: PrivoAppContainer,
    onNavigateBack: () -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit
) {
    val messages by container.chatRepository.getMessages(chatId).collectAsState(initial = emptyList())
    val currentUser by container.sessionManager.currentUserFlow.collectAsState()
    val isRecording by container.voiceRecorderHelper.isRecording.collectAsState()
    val recordingDuration by container.voiceRecorderHelper.recordedDurationSeconds.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var summaryResult by remember { mutableStateOf<AiSummaryResult?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var suggestedReplies by remember { mutableStateOf<List<String>>(emptyList()) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* open info */ }
                    ) {
                        PrivoAvatar(
                            emoji = chatEmoji,
                            colorName = "bg-indigo-600",
                            size = 40.dp,
                            showOnlineBadge = !isGroup,
                            isOnline = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chatName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGroup) "گروه اعضا" else "آنلاین",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isGroup) MaterialTheme.colorScheme.onSurfaceVariant else PrivoEmeraldSuccess
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("conversation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartCall(false) },
                        modifier = Modifier.testTag("conversation_audio_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "تماس صوتی LiveKit",
                            tint = PrivoVioletPrimary
                        )
                    }
                    IconButton(
                        onClick = { onStartCall(true) },
                        modifier = Modifier.testTag("conversation_video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "تماس تصویری LiveKit",
                            tint = PrivoVioletPrimary
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("conversation_menu_button")
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "بیشتر")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✨ خلاصه‌سازی با هوش مصنوعی") },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        val combined = messages.joinToString("\n") { "${it.senderNickname}: ${it.content}" }
                                        summaryResult = container.aiRepository.summarizeText(combined)
                                        showSummaryDialog = true
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("💡 پیشنهاد پاسخ‌های هوشمند") },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        val last = messages.lastOrNull()?.content ?: "سلام"
                                        suggestedReplies = container.aiRepository.suggestReplies(last)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🌐 ترجمه خودکار پیام‌ها") },
                                onClick = {
                                    showMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick suggested replies
                if (suggestedReplies.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestedReplies.forEach { reply ->
                            SuggestionChip(
                                onClick = {
                                    inputText = reply
                                    suggestedReplies = emptyList()
                                },
                                label = { Text(reply, maxLines = 1, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = PrivoVioletPrimary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }

                // Composer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Record or Stop
                    if (isRecording) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(PrivoRoseAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(PrivoRoseAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "در حال ضبط... $recordingDuration ثانیه",
                                    color = PrivoRoseAccent,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val file = container.voiceRecorderHelper.stopRecording()
                                    currentUser?.let { user ->
                                        scope.launch {
                                            container.chatRepository.sendMessage(
                                                chatId = chatId,
                                                currentUserId = user.id,
                                                currentUserNickname = user.nickname,
                                                content = "پیام صوتی ($recordingDuration ثانیه)",
                                                type = "voice"
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "پایان و ارسال", tint = PrivoRoseAccent)
                            }
                        }
                    } else {
                        // AI Prompt Assistant button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val last = messages.lastOrNull()?.content ?: ""
                                    suggestedReplies = container.aiRepository.suggestReplies(last)
                                }
                            },
                            modifier = Modifier.testTag("conversation_ai_suggest_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "هوش مصنوعی",
                                tint = PrivoCyanAccent
                            )
                        }

                        // Text Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("پیام شما...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("conversation_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (inputText.isNotBlank()) {
                            // Send text button
                            IconButton(
                                onClick = {
                                    val text = inputText.trim()
                                    if (text.isNotBlank()) {
                                        inputText = ""
                                        suggestedReplies = emptyList()
                                        currentUser?.let { user ->
                                            scope.launch {
                                                container.chatRepository.sendMessage(
                                                    chatId = chatId,
                                                    currentUserId = user.id,
                                                    currentUserNickname = user.nickname,
                                                    content = text,
                                                    type = "text"
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PrivoVioletPrimary)
                                    .testTag("conversation_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "ارسال",
                                    tint = Color.White
                                )
                            }
                        } else {
                            // Mic record button
                            IconButton(
                                onClick = {
                                    container.voiceRecorderHelper.startRecording()
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("conversation_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "ضبط صدا",
                                    tint = PrivoVioletPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isOutgoing = msg.senderId == currentUser?.id
                MessageBubble(
                    message = msg,
                    isOutgoing = isOutgoing,
                    isGroup = isGroup
                )
            }
        }
    }

    // AI Summary Result Dialog
    if (showSummaryDialog && summaryResult != null) {
        val result = summaryResult!!
        AlertDialog(
            onDismissRequest = { showSummaryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrivoVioletPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خلاصه هوشمند گفتگو")
                }
            },
            text = {
                Column {
                    Text(
                        text = result.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "نکات کلیدی:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrivoVioletPrimary
                    )
                    result.keyPoints.forEach { point ->
                        Text("• $point", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummaryDialog = false }) {
                    Text("متشکرم")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    isGroup: Boolean
) {
    if (message.type == "system") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val bubbleAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val isAiMessage = message.type == "ai" || message.senderId.contains("ai")

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = bubbleAlignment
    ) {
        Surface(
            shape = bubbleShape,
            color = when {
                isOutgoing -> PrivoBubbleOutgoing
                isAiMessage -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
                .widthIn(min = 60.dp, max = 300.dp)
                .then(
                    if (isAiMessage) {
                        Modifier.border(
                            1.dp,
                            Brush.linearGradient(listOf(PrivoCyanAccent, PrivoVioletPrimary)),
                            bubbleShape
                        )
                    } else Modifier
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Sender nickname in group
                if (!isOutgoing && isGroup) {
                    Text(
                        text = message.senderNickname.ifBlank { "کاربر" },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isAiMessage) PrivoCyanAccent else PrivoVioletPrimary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (isAiMessage && !isGroup) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "پرهام AI",
                            tint = PrivoCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "پرهام AI",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrivoCyanAccent
                        )
                    }
                }

                // Voice Note bubble UI
                if (message.type == "voice") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isOutgoing) Color.White.copy(alpha = 0.2f) else PrivoVioletPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "پخش صدا",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "0:12 ● ● ● ● ●",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time and Delivery Checkmark
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isOutgoing) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "تحویل داده شد",
                            tint = if (message.status == "read") PrivoCyanAccent else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
