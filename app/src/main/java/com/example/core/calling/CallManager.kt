package com.example.core.calling

import android.content.Context
import android.media.AudioManager
import com.example.domain.model.CallSession
import com.example.domain.model.CallStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CallManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

    fun startCall(
        chatId: String,
        recipientName: String,
        recipientAvatar: String,
        isVideo: Boolean
    ) {
        val call = CallSession(
            callId = "call_" + UUID.randomUUID().toString().substring(0, 8),
            chatId = chatId,
            recipientName = recipientName,
            recipientAvatar = recipientAvatar,
            isVideo = isVideo,
            status = CallStatus.CONNECTING,
            durationSeconds = 0,
            isMuted = false,
            isCameraOn = isVideo,
            isSpeakerOn = isVideo // Default speaker for video calls
        )
        _activeCall.value = call

        // Setup audio routing
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = call.isSpeakerOn

        // Simulate network connection handshake or LiveKit room connection
        scope.launch {
            delay(1500)
            _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
            startDurationTimer()
        }
    }

    private fun startDurationTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_activeCall.value?.status == CallStatus.CONNECTED) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let {
                    it.copy(durationSeconds = it.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let {
            val nextMute = !it.isMuted
            audioManager.isMicrophoneMute = nextMute
            it.copy(isMuted = nextMute)
        }
    }

    fun toggleCamera() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isCameraOn = !it.isCameraOn)
        }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let {
            val nextSpeaker = !it.isSpeakerOn
            audioManager.isSpeakerphoneOn = nextSpeaker
            it.copy(isSpeakerOn = nextSpeaker)
        }
    }

    fun endCall() {
        timerJob?.cancel()
        _activeCall.value = _activeCall.value?.copy(status = CallStatus.DISCONNECTED)
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = false
        audioManager.isMicrophoneMute = false
        scope.launch {
            delay(600)
            _activeCall.value = null
        }
    }
}
