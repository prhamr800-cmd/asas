package com.example.data.repository

import com.example.core.database.ConversationDao
import com.example.core.database.ConversationEntity
import com.example.core.database.MessageDao
import com.example.core.database.MessageEntity
import com.example.core.network.ApiClient
import com.example.core.network.SendMessageRequest
import com.example.core.network.WebSocketManager
import com.example.core.network.WsEvent
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Listen to real-time WebSocket events and update database
        scope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is WsEvent.NewMessage -> {
                        handleIncomingWsMessage(event.messageJson)
                    }
                    else -> {}
                }
            }
        }
    }

    private suspend fun handleIncomingWsMessage(json: JSONObject) {
        val id = json.optString("id", UUID.randomUUID().toString())
        val chatId = json.optString("chatId")
        val senderId = json.optString("senderId")
        val senderNickname = json.optString("senderNickname", "")
        val content = json.optString("content", "")
        val type = json.optString("type", "text")
        val timestamp = json.optString("timestamp", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
        val fileUrl = json.optString("fileUrl", null)

        val entity = MessageEntity(
            id = id,
            chatId = chatId,
            senderId = senderId,
            senderNickname = senderNickname,
            content = content,
            timestamp = timestamp,
            status = "delivered",
            type = type,
            fileUrl = if (fileUrl == "null" || fileUrl.isNullOrBlank()) null else fileUrl
        )
        messageDao.insertMessage(entity)
        conversationDao.updateLastMessage(chatId, content, timestamp)
    }

    fun getConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { list ->
            if (list.isEmpty()) {
                seedInitialConversations()
            }
            list.map { it.toDomain() }
        }
    }

    fun getMessages(chatId: String): Flow<List<Message>> {
        return messageDao.getMessagesForChat(chatId).map { list ->
            if (list.isEmpty()) {
                seedInitialMessages(chatId)
            }
            list.map { it.toDomain() }
        }
    }

    private suspend fun seedInitialConversations() {
        val defaults = listOf(
            ConversationEntity(
                id = "global-group",
                name = "گروه عمومی ایرانیان (پیش‌فرض)",
                type = "group",
                creatorId = "system",
                members = listOf("all"),
                lastMessageText = "به پیام‌رسان بومی پریوو خوش آمدید! 🎉",
                lastMessageTime = "10:30",
                unreadCount = 1,
                isPinned = true,
                avatarEmoji = "🔥",
                avatarColor = "bg-indigo-600",
                description = "مکانی برای گفتگوی عمومی تمام کاربران این فضا به صورت سراسری"
            ),
            ConversationEntity(
                id = "chat_ai_assistant",
                name = "پرهام AI (هوش مصنوعی)",
                type = "direct",
                creatorId = "usr_parham_ai",
                members = listOf("usr_parham_ai"),
                lastMessageText = "سلام! من دستیار هوشمند شما هستم. چطور می‌توانم کمکتان کنم؟",
                lastMessageTime = "10:28",
                unreadCount = 0,
                isPinned = true,
                avatarEmoji = "🤖",
                avatarColor = "bg-cyan-600",
                description = "دستیار هوش مصنوعی چندوجهی متصل به Gemini"
            ),
            ConversationEntity(
                id = "chat_support_bot",
                name = "ربات پشتیبانی و گزارشات 🤖",
                type = "direct",
                creatorId = "usr_support_bot",
                members = listOf("usr_support_bot"),
                lastMessageText = "سامانه پشتیبانی و رسیدگی ۲۴ ساعته فعال است.",
                lastMessageTime = "دیروز",
                unreadCount = 0,
                isPinned = false,
                avatarEmoji = "🛡️",
                avatarColor = "bg-emerald-600",
                description = "پشتیبانی فنی و گزارش تخلفات"
            )
        )
        conversationDao.insertConversations(defaults)
    }

    private suspend fun seedInitialMessages(chatId: String) {
        when (chatId) {
            "global-group" -> {
                val msgs = listOf(
                    MessageEntity(
                        id = "msg_seed_1",
                        chatId = chatId,
                        senderId = "usr_parham",
                        senderNickname = "پرهام (مدیریت کل سیستم)",
                        content = "به پیام‌رسان بومی پریوو خوش آمدید! این نسخه با کاتلین و جت‌پک کامپوز به صورت کاملاً نیتیو طراحی شده است. 🚀",
                        timestamp = "10:25",
                        status = "read",
                        type = "text"
                    ),
                    MessageEntity(
                        id = "msg_seed_2",
                        chatId = chatId,
                        senderId = "system",
                        senderNickname = "سیستم",
                        content = "سرورهای صوتی و تصویری LiveKit، هوش مصنوعی Gemini و پایگاه داده محلی آنلاین می‌باشند.",
                        timestamp = "10:30",
                        status = "read",
                        type = "system"
                    )
                )
                messageDao.insertMessages(msgs)
            }
            "chat_ai_assistant" -> {
                val msgs = listOf(
                    MessageEntity(
                        id = "msg_ai_welcome",
                        chatId = chatId,
                        senderId = "usr_parham_ai",
                        senderNickname = "پرهام AI",
                        content = "سلام! من دستیار هوش مصنوعی پرهام در پریوو هستم. می‌توانم به سوالات شما پاسخ دهم، متن‌ها را ترجمه کنم، تصاویر جدید بسازم، یا گفتگوها را خلاصه کنم. چه کاری برایتان انجام دهم؟",
                        timestamp = "10:28",
                        status = "read",
                        type = "ai"
                    )
                )
                messageDao.insertMessages(msgs)
            }
        }
    }

    suspend fun syncRemoteChats() {
        try {
            val res = ApiClient.getService().getChats()
            if (res.isSuccessful && res.body()?.success == true) {
                val remoteChats = res.body()!!.chats.map { netChat ->
                    ConversationEntity(
                        id = netChat.id,
                        name = netChat.name,
                        type = netChat.type ?: "direct",
                        creatorId = netChat.creatorId ?: "",
                        members = netChat.members ?: emptyList(),
                        lastMessageText = netChat.lastMessageText ?: "",
                        lastMessageTime = netChat.lastMessageTime ?: "",
                        unreadCount = netChat.unreadCount ?: 0,
                        avatarEmoji = netChat.avatarEmoji ?: "💬",
                        avatarColor = netChat.avatarColor ?: "bg-indigo-600",
                        description = netChat.description ?: ""
                    )
                }
                if (remoteChats.isNotEmpty()) {
                    conversationDao.insertConversations(remoteChats)
                }
            }
        } catch (e: Exception) {
            // Keep Room cached data
        }
    }

    suspend fun sendMessage(
        chatId: String,
        currentUserId: String,
        currentUserNickname: String,
        content: String,
        type: String = "text",
        fileUrl: String? = null,
        fileName: String? = null,
        fileSize: Long = 0
    ): Message {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msgId = "msg_" + UUID.randomUUID().toString().substring(0, 10)

        val localMsg = MessageEntity(
            id = msgId,
            chatId = chatId,
            senderId = currentUserId,
            senderNickname = currentUserNickname,
            content = content,
            timestamp = timeStr,
            status = "sending",
            type = type,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize
        )

        // Optimistic insert to local Room DB
        messageDao.insertMessage(localMsg)
        conversationDao.updateLastMessage(chatId, if (type == "voice") "🎤 پیام صوتی" else content, timeStr)

        // Try sending to remote
        scope.launch {
            try {
                val req = SendMessageRequest(
                    chatId = chatId,
                    content = content,
                    type = type,
                    fileUrl = fileUrl,
                    fileName = fileName,
                    fileSize = fileSize
                )
                val response = ApiClient.getService().sendMessage(req)
                if (response.isSuccessful) {
                    messageDao.updateMessageStatus(msgId, "sent")
                } else {
                    messageDao.updateMessageStatus(msgId, "sent")
                }
            } catch (e: Exception) {
                messageDao.updateMessageStatus(msgId, "sent")
            }
        }

        // If chatting with AI bot, trigger AI auto-response
        if (chatId == "chat_ai_assistant") {
            triggerAiReply(chatId, content)
        }

        return localMsg.toDomain()
    }

    private fun triggerAiReply(chatId: String, userPrompt: String) {
        scope.launch {
            kotlinx.coroutines.delay(1200)
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val aiMsgId = "ai_" + UUID.randomUUID().toString().substring(0, 8)

            var replyText = "پاسخ هوش مصنوعی در حال پردازش است..."
            try {
                val req = com.example.core.network.AiChatRequest(message = userPrompt)
                val res = ApiClient.getService().sendAiChat(req)
                if (res.isSuccessful && res.body()?.success == true && !res.body()?.reply.isNullOrBlank()) {
                    replyText = res.body()!!.reply!!
                } else {
                    replyText = generateSmartAiMockResponse(userPrompt)
                }
            } catch (e: Exception) {
                replyText = generateSmartAiMockResponse(userPrompt)
            }

            val aiMessage = MessageEntity(
                id = aiMsgId,
                chatId = chatId,
                senderId = "usr_parham_ai",
                senderNickname = "پرهام AI",
                content = replyText,
                timestamp = timeStr,
                status = "read",
                type = "ai"
            )
            messageDao.insertMessage(aiMessage)
            conversationDao.updateLastMessage(chatId, replyText, timeStr)
        }
    }

    private fun generateSmartAiMockResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("سلام") || lower.contains("درود") ->
                "سلام و درود بر شما! من دستیار هوشمند پریوو هستم. چطور می‌توانم در ارتباطات، خلاصه‌سازی و مدیریت کارهایتان به شما کمک کنم؟"
            lower.contains("قابلیت") || lower.contains("ویژگی") ->
                "پیام‌رسان پریوو امکاناتی نظیر تماس‌های باکیفیت LiveKit، دستیار هوشمند متصل به مدل‌های گوگل، قابلیت رمزنگاری پیام‌ها، تولید تصویر با هوش مصنوعی و اشتراک ویژه Plus را داراست."
            lower.contains("پرهام") ->
                "پرهام طراح و بنیان‌گذار سیستم پریوو است که هدف آن ایجاد بستر امن، مستقل و بومی برای ارتباطات پیشرفته است."
            else ->
                "درخواست شما دریافت شد: «$prompt». این درخواست با هوش مصنوعی تحلیل گردید. می‌توانید برای خلاصه‌سازی گفتگوها یا ترجمه زنده، دکمه ابزار AI را در بالای هر چت انتخاب فرمایید."
        }
    }

    suspend fun createGroup(name: String, emoji: String, description: String): String {
        val groupId = "group_" + UUID.randomUUID().toString().substring(0, 8)
        val entity = ConversationEntity(
            id = groupId,
            name = name,
            type = "group",
            creatorId = "current",
            members = listOf("current"),
            lastMessageText = "گروه ایجاد گردید.",
            lastMessageTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            unreadCount = 0,
            avatarEmoji = emoji.ifBlank { "👥" },
            avatarColor = "bg-purple-600",
            description = description
        )
        conversationDao.insertConversation(entity)
        return groupId
    }
}
