package com.example.domain.repository

import com.example.domain.model.CallLog
import com.example.domain.model.CallSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ICallRepository {
    val activeCall: StateFlow<CallSession?>
    val callLogs: Flow<List<CallLog>>
    fun startCall(chatId: String, recipientName: String, recipientAvatar: String, isVideo: Boolean)
    fun acceptCall()
    fun rejectCall()
    fun endCall()
    fun toggleMute(): Boolean
    fun toggleCamera(): Boolean
    fun toggleSpeaker(): Boolean
}
