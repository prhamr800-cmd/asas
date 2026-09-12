package com.example.domain.model

data class User(
    val id: String,
    val username: String,
    val nickname: String,
    val bio: String = "",
    val avatarEmoji: String = "👤",
    val avatarColor: String = "bg-indigo-600",
    val avatarUrl: String? = null,
    val role: String = "user", // "user", "admin", "owner", "assistant"
    val isOnline: Boolean = false,
    val lastSeen: String = "",
    val isTwoFactorEnabled: Boolean = false,
    val subscriptionTier: String = "free", // "free", "plus", "pro"
    val isBlocked: Boolean = false
) {
    val isOwnerOrAdmin: Boolean get() = role == "owner" || role == "admin" || username.equals("parham", ignoreCase = true)
    val isPlus: Boolean get() = subscriptionTier == "plus" || isOwnerOrAdmin
}

data class Conversation(
    val id: String,
    val name: String,
    val type: String = "direct", // "direct", "group", "channel"
    val creatorId: String = "",
    val members: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val avatarEmoji: String = "💬",
    val avatarColor: String = "bg-purple-600",
    val description: String = ""
) {
    val isGroup: Boolean get() = type == "group" || type == "channel"
}

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderNickname: String = "",
    val content: String = "",
    val timestamp: String = "",
    val status: String = "sent", // "sending", "sent", "delivered", "read"
    val type: String = "text", // "text", "image", "audio", "voice", "video", "file", "system", "ai"
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val replyToId: String? = null,
    val isPinned: Boolean = false,
    val translation: String? = null
)

data class CallSession(
    val callId: String,
    val chatId: String,
    val recipientName: String,
    val recipientAvatar: String,
    val isVideo: Boolean = false,
    val status: CallStatus = CallStatus.CONNECTING,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isSpeakerOn: Boolean = false
)

enum class CallStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    FAILED
}

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val period: String,
    val features: List<String>,
    val isRecommended: Boolean = false
)

data class AiSummaryResult(
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val actionItems: List<String> = emptyList(),
    val sentiment: String = "Neutral"
)

data class AiMessage(
    val id: String,
    val role: String, // "user", "model", "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
