package com.example.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val twoFactorCode: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val user: NetworkUser? = null,
    val sessionId: String? = null,
    val requiresTwoFactor: Boolean = false,
    val tempToken: String? = null
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val nickname: String,
    val password: String,
    val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkUser(
    val id: String,
    val username: String,
    val nickname: String? = null,
    val bio: String? = null,
    val avatarColor: String? = null,
    val avatarEmoji: String? = null,
    val avatarUrl: String? = null,
    val role: String? = null,
    val isOnline: Boolean? = null,
    val lastSeen: String? = null,
    val isTwoFactorEnabled: Boolean? = null,
    val subscriptionTier: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val nickname: String,
    val bio: String,
    val avatarEmoji: String,
    val avatarColor: String
)

@JsonClass(generateAdapter = true)
data class ChatsResponse(
    val success: Boolean = false,
    val chats: List<NetworkChat> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NetworkChat(
    val id: String,
    val name: String,
    val type: String? = "direct",
    val creatorId: String? = "",
    val members: List<String>? = emptyList(),
    val lastMessageText: String? = "",
    val lastMessageTime: String? = "",
    val unreadCount: Int? = 0,
    val avatarEmoji: String? = "💬",
    val avatarColor: String? = "bg-indigo-600",
    val description: String? = ""
)

@JsonClass(generateAdapter = true)
data class MessagesResponse(
    val success: Boolean = false,
    val messages: List<NetworkMessage> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NetworkMessage(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderNickname: String? = "",
    val content: String? = "",
    val timestamp: String? = "",
    val status: String? = "sent",
    val type: String? = "text",
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = 0,
    val replyToId: String? = null,
    val isPinned: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val chatId: String,
    val content: String,
    val type: String = "text",
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = 0,
    val replyToId: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageResponse(
    val success: Boolean = false,
    val message: NetworkMessage? = null
)

@JsonClass(generateAdapter = true)
data class LiveKitTokenRequest(
    val chatId: String,
    val participantName: String,
    val participantId: String,
    val isAudioOnly: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LiveKitTokenResponse(
    val success: Boolean = false,
    val token: String? = null,
    val url: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AiChatRequest(
    val message: String,
    val history: List<AiHistoryItem>? = null,
    val systemPrompt: String? = null
)

@JsonClass(generateAdapter = true)
data class AiHistoryItem(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class AiChatResponse(
    val success: Boolean = false,
    val reply: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AiSummarizeRequest(
    val text: String
)

@JsonClass(generateAdapter = true)
data class AiSummarizeResponse(
    val success: Boolean = false,
    val summary: String? = null,
    val keyPoints: List<String>? = emptyList(),
    val actionItems: List<String>? = emptyList(),
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AiTranslateRequest(
    val text: String,
    val targetLanguage: String
)

@JsonClass(generateAdapter = true)
data class AiTranslateResponse(
    val success: Boolean = false,
    val translatedText: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AiSuggestRepliesRequest(
    val lastMessage: String,
    val context: String? = null
)

@JsonClass(generateAdapter = true)
data class AiSuggestRepliesResponse(
    val success: Boolean = false,
    val suggestions: List<String>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class AiGenerateImageRequest(
    val prompt: String,
    val style: String? = "futuristic"
)

@JsonClass(generateAdapter = true)
data class AiGenerateImageResponse(
    val success: Boolean = false,
    val imageUrl: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class PurchaseSubscriptionRequest(
    val tier: String,
    val plan: String
)

@JsonClass(generateAdapter = true)
data class AdminBroadcastRequest(
    val message: String,
    val title: String? = "اعلان سراسری سیستم"
)

@JsonClass(generateAdapter = true)
data class AdminMetricsResponse(
    val success: Boolean = false,
    val totalUsers: Int = 0,
    val onlineUsers: Int = 0,
    val totalMessages: Int = 0,
    val totalChats: Int = 0,
    val activeCalls: Int = 0
)

@JsonClass(generateAdapter = true)
data class RequestOtpRequest(val username: String)
@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(val username: String, val otp: String)
@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(val username: String, val resetToken: String, val newPassword: String)
