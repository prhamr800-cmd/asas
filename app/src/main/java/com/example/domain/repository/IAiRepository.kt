package com.example.domain.repository

import com.example.domain.model.AiMessage
import com.example.domain.model.AiSummaryResult

interface IAiRepository {
    suspend fun sendChatMessage(userText: String): Result<String>
    suspend fun summarizeConversation(chatId: String, messagesText: String): Result<AiSummaryResult>
    suspend fun translateMessage(text: String, targetLanguage: String = "Persian"): Result<String>
    suspend fun generateSuggestedReplies(lastMessage: String): Result<List<String>>
    suspend fun generateImage(prompt: String, style: String = "photorealistic"): Result<String>
    fun getAssistantHistory(): List<AiMessage>
}
