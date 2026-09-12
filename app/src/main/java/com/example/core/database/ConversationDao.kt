package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :chatId")
    fun getConversationById(chatId: String): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET lastMessageText = :text, lastMessageTime = :time WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, text: String, time: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :chatId")
    suspend fun resetUnreadCount(chatId: String)

    @Query("DELETE FROM conversations WHERE id = :chatId")
    suspend fun deleteConversation(chatId: String)

    @Query("DELETE FROM conversations")
    suspend fun clearConversations()
}
