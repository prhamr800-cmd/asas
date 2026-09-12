package com.example.data.repository

import com.example.core.database.ConversationDao
import com.example.core.database.ConversationEntity
import com.example.core.database.MessageDao
import com.example.core.database.MessageEntity
import com.example.core.firebase.FirebaseManager
import com.example.core.network.ApiClient
import com.example.core.network.SendMessageRequest
import com.example.core.network.WebSocketManager
import com.example.core.network.WsEvent
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.domain.repository.IChatRepository
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
    private val webSocketManager: WebSocketManager,
    private val firebaseManager: FirebaseManager
) : IChatRepository {
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

        // Listen to real-time Firestore Chats and sync to Room
        scope.launch {
            try {
                firebaseManager.listenToChats().collect { firestoreChats ->
                    if (firestoreChats.isNotEmpty()) {
                        val entities = firestoreChats.map { ConversationEntity.fromDomain(it) }
                        conversationDao.insertConversations(entities)
                    }
                }
            } catch (e: Exception) {
                // Keep local
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
        // Listen to Firestore real-time messages for this chat in the background
        scope.launch {
            try {
                firebaseManager.listenToMessages(chatId).collect { fsMessages ->
                    if (fsMessages.isNotEmpty()) {
                        val entities = fsMessages.map { MessageEntity.fromDomain(it) }
                        messageDao.insertMessages(entities)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

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

        // Seed to Firestore as well
        defaults.forEach {
            firebaseManager.createChatInFirestore(it.toDomain())
        }
    }

    private suspend fun seedInitialMessages(chatId: String) {
        when (chatId) {
            "global-group" -> {
                val msgs = listOf(
                    MessageEntity(
                        id = "m1",
                        chatId = chatId,
                        senderId = "usr_parham",
                        senderNickname = "پرهام (مدیر کل)",
                        content = "درود به همگی! نسخه جدید پیام‌رسان پریوو با پشتیبانی از فایربیس، هوش مصنوعی Gemini و تماس‌های صوتی و تصویری آماده شد.",
                        timestamp = "10:25",
                        status = "read",
                        type = "text"
                    ),
                    MessageEntity(
                        id = "m2",
                        chatId = chatId,
                        senderId = "usr_sara",
                        senderNickname = "سارا مهدوی",
                        content = "سلام آقای مهندس، طراحی برنامه فوق‌العاده سریع و روونه!",
                        timestamp = "10:28",
                        status = "read",
                        type = "text"
                    ),
                    MessageEntity(
                        id = "m3",
                        chatId = chatId,
                        senderId = "system",
                        senderNickname = "پریوو سیستم",
                        content = "به پیام‌رسان بومی پریوو خوش آمدید! 🎉",
                        timestamp = "10:30",
                        status = "read",
                        type = "text"
                    )
                )
                messageDao.insertMessages(msgs)
                msgs.forEach { firebaseManager.sendMessageToFirestore(it.toDomain()) }
            }
            "chat_ai_assistant" -> {
                val msgs = listOf(
                    MessageEntity(
                        id = "ai_welcome",
                        chatId = chatId,
                        senderId = "usr_parham_ai",
                        senderNickname = "پرهام AI",
                        content = "سلام! من دستیار هوشمند پریوو هستم. می‌توانم به سوالات شما پاسخ دهم، گفتگوها را خلاصه کنم و تصاویر تولید کنم.",
                        timestamp = "10:28",
                        status = "read",
                        type = "text"
                    )
                )
                messageDao.insertMessages(msgs)
            }
            "chat_support_bot" -> {
                val msgs = listOf(
                    MessageEntity(
                        id = "bot_welcome",
                        chatId = chatId,
                        senderId = "usr_support_bot",
                        senderNickname = "پشتیبانی پریوو",
                        content = "درود! برای ثبت هرگونه پیشنهاد، انتقاد یا گزارش تخلف می‌توانید پیام خود را در اینجا ارسال فرمایید.",
                        timestamp = "دیروز",
                        status = "read",
                        type = "text"
                    )
                )
                messageDao.insertMessages(msgs)
            }
        }
    }

    suspend fun refreshConversations() {
        try {
            val response = ApiClient.getService().getChats()
            if (response.isSuccessful && response.body() != null) {
                val remoteChats = response.body()!!.map { netChat ->
                    ConversationEntity(
                        id = netChat.id,
                        name = netChat.name,
                        type = netChat.type,
                        creatorId = netChat.creatorId ?: "",
                        members = netChat.members,
                        lastMessageText = netChat.lastMessageText ?: "",
                        lastMessageTime = netChat.lastMessageTime ?: "",
                        unreadCount = netChat.unreadCount,
                        isPinned = netChat.isPinned,
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

        // Sync to Firestore cloud database
        scope.launch {
            firebaseManager.sendMessageToFirestore(localMsg.toDomain())
        }

        // Try sending to REST backend
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
                    replyText = "پاسخ از طرف دستیار هوشمند پریوو:\nپیام شما دریافت شد. من آماده پاسخگویی به هرگونه درخواست یا راهنمایی هستم."
                }
            } catch (e: Exception) {
                replyText = "پاسخ آفلاین پریوو AI:\nپیام شما را دریافت کردم. ارتباط من با سرور موقتاً در وضعیت آفلاین است، اما تمام امکانات محلی در دسترس هستند."
            }

            val aiMsg = MessageEntity(
                id = aiMsgId,
                chatId = chatId,
                senderId = "usr_parham_ai",
                senderNickname = "پرهام AI",
                content = replyText,
                timestamp = timeStr,
                status = "read",
                type = "text"
            )
            messageDao.insertMessage(aiMsg)
            conversationDao.updateLastMessage(chatId, replyText, timeStr)
            firebaseManager.sendMessageToFirestore(aiMsg.toDomain())
        }
    }

    suspend fun createGroup(name: String, description: String, creatorId: String): Conversation {
        val groupId = "group_" + UUID.randomUUID().toString().substring(0, 8)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val colors = listOf("bg-blue-600", "bg-purple-600", "bg-emerald-600", "bg-amber-600", "bg-rose-600")
        val emojis = listOf("👥", "🚀", "⚡", "🌟", "💡", "🎯")

        val newGroup = ConversationEntity(
            id = groupId,
            name = name,
            type = "group",
            creatorId = creatorId,
            members = listOf(creatorId),
            lastMessageText = "گروه ایجاد گردید.",
            lastMessageTime = timeStr,
            unreadCount = 0,
            isPinned = false,
            avatarEmoji = emojis.random(),
            avatarColor = colors.random(),
            description = description
        )

        conversationDao.insertConversation(newGroup)
        firebaseManager.createChatInFirestore(newGroup.toDomain())

        // Also notify backend
        scope.launch {
            try {
                ApiClient.getService().createChat(
                    com.example.core.network.CreateChatRequest(
                        name = name,
                        type = "group",
                        description = description,
                        memberIds = listOf(creatorId)
                    )
                )
            } catch (e: Exception) {
                // Saved locally and in Firestore
            }
        }

        return newGroup.toDomain()
    }
}
