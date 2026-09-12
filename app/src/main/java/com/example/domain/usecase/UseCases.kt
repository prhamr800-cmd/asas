package com.example.domain.usecase

import com.example.domain.model.AiSummaryResult
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.domain.model.User
import com.example.domain.repository.IAiRepository
import com.example.domain.repository.IAuthRepository
import com.example.domain.repository.IChatRepository
import kotlinx.coroutines.flow.Flow

class GetConversationsUseCase(private val chatRepository: IChatRepository) {
    operator fun invoke(): Flow<List<Conversation>> = chatRepository.getConversations()
}

class GetMessagesUseCase(private val chatRepository: IChatRepository) {
    operator fun invoke(chatId: String): Flow<List<Message>> = chatRepository.getMessages(chatId)
}

class SendMessageUseCase(private val chatRepository: IChatRepository) {
    suspend operator fun invoke(
        chatId: String,
        currentUserId: String,
        currentUserNickname: String,
        content: String,
        type: String = "text",
        fileUrl: String? = null,
        fileName: String? = null,
        fileSize: Long = 0
    ): Message {
        return chatRepository.sendMessage(
            chatId = chatId,
            currentUserId = currentUserId,
            currentUserNickname = currentUserNickname,
            content = content,
            type = type,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize
        )
    }
}

class CreateGroupUseCase(private val chatRepository: IChatRepository) {
    suspend operator fun invoke(name: String, description: String, creatorId: String): Conversation {
        return chatRepository.createGroup(name, description, creatorId)
    }
}

class LoginUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(username: String, pass: String, twoFactorCode: String? = null): Result<User> {
        return authRepository.login(username, pass, twoFactorCode)
    }
}

class RegisterUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(username: String, nickname: String, pass: String, bio: String): Result<User> {
        return authRepository.register(username, nickname, pass, bio)
    }
}

class SummarizeChatUseCase(private val aiRepository: IAiRepository) {
    suspend operator fun invoke(chatId: String, messagesText: String): Result<AiSummaryResult> {
        return aiRepository.summarizeConversation(chatId, messagesText)
    }
}
