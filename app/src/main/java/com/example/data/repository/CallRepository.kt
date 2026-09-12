package com.example.data.repository

import com.example.core.calling.CallManager
import com.example.core.database.CallDao
import com.example.core.database.CallEntity
import com.example.domain.model.CallLog
import com.example.domain.model.CallSession
import com.example.domain.repository.ICallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallRepository(
    private val callManager: CallManager,
    private val callDao: CallDao
) : ICallRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    override val activeCall: StateFlow<CallSession?> = callManager.activeCall

    override val callLogs: Flow<List<CallLog>> = callDao.getAllCalls().map { list ->
        list.map { it.toDomain() }
    }

    override fun startCall(
        chatId: String,
        recipientName: String,
        recipientAvatar: String,
        isVideo: Boolean
    ) {
        callManager.startCall(chatId, recipientName, recipientAvatar, isVideo)
    }

    override fun acceptCall() {
        callManager.acceptIncomingCall()
    }

    override fun rejectCall() {
        callManager.rejectIncomingCall()
    }

    override fun endCall() {
        val current = callManager.activeCall.value
        if (current != null) {
            val nowStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val log = CallEntity(
                id = current.callId,
                contactName = current.recipientName,
                contactAvatar = current.recipientAvatar,
                isVideo = current.isVideo,
                isIncoming = false,
                timestamp = nowStr,
                durationSeconds = current.durationSeconds,
                status = "completed"
            )
            scope.launch {
                callDao.insertCall(log)
            }
        }
        callManager.endCall()
    }

    override fun toggleMute(): Boolean = callManager.toggleMute()

    override fun toggleCamera(): Boolean = callManager.toggleCamera()

    override fun toggleSpeaker(): Boolean = callManager.toggleSpeaker()
}
