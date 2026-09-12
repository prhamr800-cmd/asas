package com.example.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.domain.model.PendingMessage
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pending_messages")
data class PendingMessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val content: String,
    val type: String,
    val timestamp: String,
    val retryCount: Int = 0
) {
    fun toDomain(): PendingMessage = PendingMessage(
        id = id,
        chatId = chatId,
        content = content,
        type = type,
        timestamp = timestamp,
        retryCount = retryCount
    )

    companion object {
        fun fromDomain(msg: PendingMessage): PendingMessageEntity = PendingMessageEntity(
            id = msg.id,
            chatId = msg.chatId,
            content = msg.content,
            type = msg.type,
            timestamp = msg.timestamp,
            retryCount = msg.retryCount
        )
    }
}

@Dao
interface PendingMessageDao {
    @Query("SELECT * FROM pending_messages ORDER BY timestamp ASC")
    suspend fun getAllPending(): List<PendingMessageEntity>

    @Query("SELECT * FROM pending_messages WHERE chatId = :chatId")
    fun getPendingForChat(chatId: String): Flow<List<PendingMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(msg: PendingMessageEntity)

    @Query("DELETE FROM pending_messages WHERE id = :id")
    suspend fun deletePending(id: String)
}
