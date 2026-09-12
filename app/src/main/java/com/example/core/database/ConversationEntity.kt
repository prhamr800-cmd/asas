package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Conversation

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String = "direct",
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
    fun toDomain(): Conversation = Conversation(
        id = id,
        name = name,
        type = type,
        creatorId = creatorId,
        members = members,
        lastMessageText = lastMessageText,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        avatarEmoji = avatarEmoji,
        avatarColor = avatarColor,
        description = description
    )

    companion object {
        fun fromDomain(c: Conversation): ConversationEntity = ConversationEntity(
            id = c.id,
            name = c.name,
            type = c.type,
            creatorId = c.creatorId,
            members = c.members,
            lastMessageText = c.lastMessageText,
            lastMessageTime = c.lastMessageTime,
            unreadCount = c.unreadCount,
            isPinned = c.isPinned,
            isMuted = c.isMuted,
            avatarEmoji = c.avatarEmoji,
            avatarColor = c.avatarColor,
            description = c.description
        )
    }
}
