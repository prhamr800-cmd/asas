package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Group

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val creatorId: String,
    val members: List<String>,
    val adminIds: List<String>,
    val avatarEmoji: String,
    val avatarColor: String,
    val createdAtEpoch: Long
) {
    fun toDomain(): Group = Group(
        id = id,
        name = name,
        description = description,
        creatorId = creatorId,
        memberIds = members,
        adminIds = adminIds,
        avatarEmoji = avatarEmoji,
        avatarColor = avatarColor,
        createdAtEpoch = createdAtEpoch
    )

    companion object {
        fun fromDomain(group: Group): GroupEntity = GroupEntity(
            id = group.id,
            name = group.name,
            description = group.description,
            creatorId = group.creatorId,
            members = group.memberIds,
            adminIds = group.adminIds,
            avatarEmoji = group.avatarEmoji,
            avatarColor = group.avatarColor,
            createdAtEpoch = group.createdAtEpoch
        )
    }
}
