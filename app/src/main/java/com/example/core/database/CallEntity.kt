package com.example.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.domain.model.CallLog
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "call_logs")
data class CallEntity(
    @PrimaryKey
    val id: String,
    val contactName: String,
    val contactAvatar: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val timestamp: String,
    val durationSeconds: Int,
    val status: String
) {
    fun toDomain(): CallLog = CallLog(
        id = id,
        contactName = contactName,
        contactAvatar = contactAvatar,
        isVideo = isVideo,
        isIncoming = isIncoming,
        timestamp = timestamp,
        durationSeconds = durationSeconds,
        status = status
    )

    companion object {
        fun fromDomain(log: CallLog): CallEntity = CallEntity(
            id = log.id,
            contactName = log.contactName,
            contactAvatar = log.contactAvatar,
            isVideo = log.isVideo,
            isIncoming = log.isIncoming,
            timestamp = log.timestamp,
            durationSeconds = log.durationSeconds,
            status = log.status
        )
    }
}

@Dao
interface CallDao {
    @Query("SELECT * FROM call_logs ORDER BY id DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallEntity>)

    @Query("DELETE FROM call_logs")
    suspend fun clearCallLogs()
}
