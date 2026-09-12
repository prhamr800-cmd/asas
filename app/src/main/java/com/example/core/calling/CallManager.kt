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
    private val liveKitService = LiveKitService(context)

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
            isSpeakerOn = isVideo
        )
        _activeCall.value = call

        // Setup audio routing
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = call.isSpeakerOn

        liveKitService.connectToRoom(
            roomName = "room_${chatId}",
            participantIdentity = "me",
            isVideo = isVideo
        )

        scope.launch {
            delay(1500)
            _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
            startDurationTimer()
        }
    }

    fun acceptIncomingCall() {
        _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
        startDurationTimer()
    }

    fun rejectIncomingCall() {
        endCall()
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

    fun toggleMute(): Boolean {
        var isNowMuted = false
        _activeCall.value = _activeCall.value?.let {
            val nextMute = !it.isMuted
            audioManager.isMicrophoneMute = nextMute
            isNowMuted = nextMute
            it.copy(isMuted = nextMute)
        }
        return isNowMuted
    }

    fun toggleCamera(): Boolean {
        var isNowCameraOn = true
        _activeCall.value = _activeCall.value?.let {
            val nextCamera = !it.isCameraOn
            isNowCameraOn = nextCamera
            it.copy(isCameraOn = nextCamera)
        }
        return isNowCameraOn
    }

    fun toggleSpeaker(): Boolean {
        var isNowSpeaker = false
        _activeCall.value = _activeCall.value?.let {
            val nextSpeaker = !it.isSpeakerOn
            audioManager.isSpeakerphoneOn = nextSpeaker
            isNowSpeaker = nextSpeaker
            it.copy(isSpeakerOn = nextSpeaker)
        }
        return isNowSpeaker
    }

    fun endCall() {
        timerJob?.cancel()
        liveKitService.disconnect()
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
