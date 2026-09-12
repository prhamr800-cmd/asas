package com.example.domain.repository

import com.example.domain.model.Conversation
import com.example.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface IChatRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun refreshConversations()
    suspend fun sendMessage(
        chatId: String,
        currentUserId: String,
        currentUserNickname: String,
        content: String,
        type: String = "text",
        fileUrl: String? = null,
        fileName: String? = null,
        fileSize: Long = 0
    ): Message
    suspend fun createGroup(name: String, description: String, creatorId: String): Conversation
}
