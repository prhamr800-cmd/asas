package com.example

import android.content.Context
import com.example.core.calling.CallManager
import com.example.core.database.PrivoDatabase
import com.example.core.media.VoiceRecorderHelper
import com.example.core.network.ApiClient
import com.example.core.network.WebSocketManager
import com.example.core.security.SessionManager
import com.example.data.repository.AdminRepository
import com.example.data.repository.AiRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.SubscriptionRepository

class PrivoAppContainer(private val context: Context) {
    val sessionManager: SessionManager by lazy {
        SessionManager(context)
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

    val voiceRecorderHelper: VoiceRecorderHelper by lazy {
        VoiceRecorderHelper(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(sessionManager, database.userDao())
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.conversationDao(), database.messageDao(), webSocketManager)
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

    init {
        ApiClient.init(sessionManager)
    }
}
