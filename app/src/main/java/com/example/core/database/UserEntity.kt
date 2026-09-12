package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val nickname: String,
    val bio: String = "",
    val avatarEmoji: String = "👤",
    val avatarColor: String = "bg-indigo-600",
    val avatarUrl: String? = null,
    val role: String = "user",
    val isOnline: Boolean = false,
    val lastSeen: String = "",
    val isTwoFactorEnabled: Boolean = false,
    val subscriptionTier: String = "free",
    val isBlocked: Boolean = false
) {
    fun toDomain(): User = User(
        id = id,
        username = username,
        nickname = nickname,
        bio = bio,
        avatarEmoji = avatarEmoji,
        avatarColor = avatarColor,
        avatarUrl = avatarUrl,
        role = role,
        isOnline = isOnline,
        lastSeen = lastSeen,
        isTwoFactorEnabled = isTwoFactorEnabled,
        subscriptionTier = subscriptionTier,
        isBlocked = isBlocked
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            username = user.username,
            nickname = user.nickname,
            bio = user.bio,
            avatarEmoji = user.avatarEmoji,
            avatarColor = user.avatarColor,
            avatarUrl = user.avatarUrl,
            role = user.role,
            isOnline = user.isOnline,
            lastSeen = user.lastSeen,
            isTwoFactorEnabled = user.isTwoFactorEnabled,
            subscriptionTier = user.subscriptionTier,
            isBlocked = user.isBlocked
        )
    }
}
