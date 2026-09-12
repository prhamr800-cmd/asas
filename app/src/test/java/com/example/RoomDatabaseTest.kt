package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.CallDao
import com.example.core.database.CallEntity
import com.example.core.database.ConversationDao
import com.example.core.database.ConversationEntity
import com.example.core.database.GroupDao
import com.example.core.database.GroupEntity
import com.example.core.database.MessageDao
import com.example.core.database.MessageEntity
import com.example.core.database.PrivoDatabase
import com.example.core.database.UserDao
import com.example.core.database.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDatabaseTest {

    private lateinit var db: PrivoDatabase
    private lateinit var userDao: UserDao
    private lateinit var convDao: ConversationDao
    private lateinit var msgDao: MessageDao
    private lateinit var groupDao: GroupDao
    private lateinit var callDao: CallDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PrivoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        convDao = db.conversationDao()
        msgDao = db.messageDao()
        groupDao = db.groupDao()
        callDao = db.callDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveUser() = runBlocking {
        val user = UserEntity(
            id = "u1",
            username = "parham",
            nickname = "پرهام",
            bio = "توسعه‌دهنده پریوو",
            role = "owner"
        )
        userDao.insertUser(user)
        val loaded = userDao.getUserById("u1")
        assertNotNull(loaded)
        assertEquals("parham", loaded?.username)
        assertEquals("پرهام", loaded?.nickname)
        assertEquals("owner", loaded?.role)
    }

    @Test
    fun insertAndRetrieveConversations() = runBlocking {
        val conv = ConversationEntity(
            id = "conv1",
            name = "تیم پریوو",
            type = "group",
            lastMessageText = "سلام به همه اعضا",
            lastMessageTime = "12:00",
            unreadCount = 2,
            isPinned = true
        )
        convDao.insertConversation(conv)
        val list = convDao.getAllConversations().first()
        assertTrue(list.isNotEmpty())
        assertEquals("conv1", list[0].id)
        assertEquals("تیم پریوو", list[0].name)
    }

    @Test
    fun insertAndRetrieveMessages() = runBlocking {
        val msg = MessageEntity(
            id = "msg1",
            chatId = "conv1",
            senderId = "u1",
            senderNickname = "پرهام",
            content = "پیام آزمایشی رمزنگاری شده",
            timestamp = "12:05",
            status = "sent",
            type = "text"
        )
        msgDao.insertMessage(msg)
        val messages = msgDao.getMessagesForChat("conv1").first()
        assertEquals(1, messages.size)
        assertEquals("پیام آزمایشی رمزنگاری شده", messages[0].content)
    }

    @Test
    fun insertAndRetrieveGroup() = runBlocking {
        val group = GroupEntity(
            id = "grp1",
            name = "توسعه‌دهندگان",
            description = "گروه مهندسی نیتیو اندروید",
            creatorId = "u1",
            members = listOf("u1", "u2"),
            adminIds = listOf("u1"),
            avatarEmoji = "💻",
            avatarColor = "bg-blue-600",
            createdAtEpoch = System.currentTimeMillis()
        )
        groupDao.insertGroup(group)
        val retrieved = groupDao.getGroupById("grp1")
        assertNotNull(retrieved)
        assertEquals("توسعه‌دهندگان", retrieved?.name)
        assertEquals(2, retrieved?.members?.size)
    }

    @Test
    fun insertAndRetrieveCallLogs() = runBlocking {
        val call = CallEntity(
            id = "call1",
            contactName = "علی",
            contactAvatar = "👤",
            isVideo = true,
            isIncoming = false,
            timestamp = "14:30",
            durationSeconds = 145,
            status = "completed"
        )
        callDao.insertCall(call)
        val calls = callDao.getAllCalls().first()
        assertEquals(1, calls.size)
        assertEquals("علی", calls[0].contactName)
        assertTrue(calls[0].isVideo)
    }
}
