package com.example

import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.domain.model.User
import com.example.domain.repository.IChatRepository
import com.example.domain.usecase.CreateGroupUseCase
import com.example.domain.usecase.GetConversationsUseCase
import com.example.domain.usecase.GetMessagesUseCase
import com.example.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeChatRepository : IChatRepository {
    private val conversations = mutableListOf<Conversation>()
    private val messages = mutableListOf<Message>()

    override fun getConversations(): Flow<List<Conversation>> = flowOf(conversations)

    override fun getMessages(chatId: String): Flow<List<Message>> =
        flowOf(messages.filter { it.chatId == chatId })

    override suspend fun refreshConversations() {}

    override suspend fun sendMessage(
        chatId: String,
        currentUserId: String,
        currentUserNickname: String,
        content: String,
        type: String,
        fileUrl: String?,
        fileName: String?,
        fileSize: Long
    ): Message {
        val msg = Message(
            id = "msg_${messages.size + 1}",
            chatId = chatId,
            senderId = currentUserId,
            senderNickname = currentUserNickname,
            content = content,
            timestamp = "12:00",
            status = "sent",
            type = type,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize
        )
        messages.add(msg)
        return msg
    }

    override suspend fun createGroup(name: String, description: String, creatorId: String): Conversation {
        val conv = Conversation(
            id = "grp_${conversations.size + 1}",
            name = name,
            type = "group",
            description = description,
            creatorId = creatorId
        )
        conversations.add(conv)
        return conv
    }
}

class UseCasesTest {

    @Test
    fun testSendMessageUseCase() = runBlocking {
        val fakeRepo = FakeChatRepository()
        val sendMessageUseCase = SendMessageUseCase(fakeRepo)
        val getMessagesUseCase = GetMessagesUseCase(fakeRepo)

        val sent = sendMessageUseCase(
            chatId = "chat_1",
            currentUserId = "user_1",
            currentUserNickname = "پرهام",
            content = "درود بر شما",
            type = "text"
        )

        assertEquals("درود بر شما", sent.content)
        assertEquals("user_1", sent.senderId)

        val list = getMessagesUseCase("chat_1").first()
        assertEquals(1, list.size)
        assertEquals("درود بر شما", list[0].content)
    }

    @Test
    fun testCreateGroupUseCase() = runBlocking {
        val fakeRepo = FakeChatRepository()
        val createGroupUseCase = CreateGroupUseCase(fakeRepo)
        val getConversationsUseCase = GetConversationsUseCase(fakeRepo)

        val group = createGroupUseCase(
            name = "گروه مهندسی نرم‌افزار",
            description = "تیم پریوو",
            creatorId = "user_1"
        )

        assertEquals("گروه مهندسی نرم‌افزار", group.name)
        assertEquals("group", group.type)

        val list = getConversationsUseCase().first()
        assertEquals(1, list.size)
        assertEquals("گروه مهندسی نرم‌افزار", list[0].name)
    }
}
