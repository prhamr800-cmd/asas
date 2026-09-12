package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Message

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderNickname: String = "",
    val content: String = "",
    val timestamp: String = "",
    val status: String = "sent",
    val type: String = "text",
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val replyToId: String? = null,
    val isPinned: Boolean = false,
    val translation: String? = null
) {
    fun toDomain(): Message = Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderNickname = senderNickname,
        content = content,
        timestamp = timestamp,
        status = status,
        type = type,
        fileUrl = fileUrl,
        fileName = fileName,
        fileSize = fileSize,
        replyToId = replyToId,
        isPinned = isPinned,
        translation = translation
    )

    companion object {
        fun fromDomain(m: Message): MessageEntity = MessageEntity(
            id = m.id,
            chatId = m.chatId,
            senderId = m.senderId,
            senderNickname = m.senderNickname,
            content = m.content,
            timestamp = m.timestamp,
            status = m.status,
            type = m.type,
            fileUrl = m.fileUrl,
            fileName = m.fileName,
            fileSize = m.fileSize,
            replyToId = m.replyToId,
            isPinned = m.isPinned,
            translation = m.translation
        )
    }
}
