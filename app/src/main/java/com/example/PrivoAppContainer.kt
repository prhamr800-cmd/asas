package com.example

import android.content.Context
import com.example.core.calling.CallManager
import com.example.core.database.PrivoDatabase
import com.example.core.firebase.FirebaseManager
import com.example.core.media.VoiceRecorderHelper
import com.example.core.network.ApiClient
import com.example.core.network.WebSocketManager
import com.example.core.security.SessionManager
import com.example.data.repository.AdminRepository
import com.example.data.repository.AiRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.SubscriptionRepository
import com.example.domain.usecase.CreateGroupUseCase
import com.example.domain.usecase.GetConversationsUseCase
import com.example.domain.usecase.GetMessagesUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.RegisterUseCase
import com.example.domain.usecase.SendMessageUseCase
import com.example.domain.usecase.SummarizeChatUseCase

class PrivoAppContainer(private val context: Context) {
    val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    val firebaseManager: FirebaseManager by lazy {
        FirebaseManager(context)
    }

    val database: PrivoDatabase by lazy {
        PrivoDatabase.getInstance(context)
    }

    val webSocketManager: WebSocketManager by lazy {
        WebSocketManager()
    }

    val callManager: CallManager by lazy {
        CallManager(context)
    }

    val callRepository: CallRepository by lazy {
        CallRepository(callManager, database.callDao())
    }

    val voiceRecorderHelper: VoiceRecorderHelper by lazy {
        VoiceRecorderHelper(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(sessionManager, database.userDao(), firebaseManager)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.conversationDao(), database.messageDao(), webSocketManager, firebaseManager)
    }

    val aiRepository: AiRepository by lazy {
        AiRepository()
    }

    val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepository(sessionManager)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepository()
    }

    // Domain Use Cases
    val getConversationsUseCase: GetConversationsUseCase by lazy {
        GetConversationsUseCase(chatRepository)
    }

    val getMessagesUseCase: GetMessagesUseCase by lazy {
        GetMessagesUseCase(chatRepository)
    }

    val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(chatRepository)
    }

    val createGroupUseCase: CreateGroupUseCase by lazy {
        CreateGroupUseCase(chatRepository)
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val summarizeChatUseCase: SummarizeChatUseCase by lazy {
        SummarizeChatUseCase(aiRepository)
    }

    init {
        ApiClient.init(sessionManager)
    }
}
