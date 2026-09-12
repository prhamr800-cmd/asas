package com.example.core.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

sealed class WsEvent {
    data class NewMessage(val messageJson: JSONObject) : WsEvent()
    data class UserTyping(val chatId: String, val userId: String, val isTyping: Boolean) : WsEvent()
    data class UserPresence(val userId: String, val isOnline: Boolean) : WsEvent()
    data class CallOffer(val callId: String, val fromUserId: String, val chatId: String, val isVideo: Boolean) : WsEvent()
    data class CallEnded(val callId: String) : WsEvent()
    data class ConnectionState(val isConnected: Boolean) : WsEvent()
}

class WebSocketManager(
    private val baseWsUrl: String = ApiClient.WS_URL
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var shouldReconnect = true
    private var currentUserId: String? = null
    private var currentSessionId: String? = null

    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events.asSharedFlow()

    fun connect(userId: String, sessionId: String) {
        currentUserId = userId
        currentSessionId = sessionId
        shouldReconnect = true
        initiateConnection()
    }

    private fun initiateConnection() {
        try {
            val url = "$baseWsUrl?userId=$currentUserId&sessionId=$currentSessionId"
            val request = Request.Builder().url(url).build()

            webSocket = ApiClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("PrivoWS", "WebSocket connected successfully.")
                    scope.launch { _events.emit(WsEvent.ConnectionState(true)) }
                    // Send auth handshake
                    val authObj = JSONObject().apply {
                        put("type", "auth")
                        put("userId", currentUserId)
                        put("sessionId", currentSessionId)
                    }
                    webSocket.send(authObj.toString())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)
                    scope.launch { _events.emit(WsEvent.ConnectionState(false)) }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("PrivoWS", "WebSocket error: ${t.message}")
                    scope.launch {
                        _events.emit(WsEvent.ConnectionState(false))
                        if (shouldReconnect) {
                            delay(4000)
                            initiateConnection()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("PrivoWS", "Error establishing WebSocket connection", e)
        }
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val json = JSONObject(text)
            val type = json.optString("type")
            val payload = json.optJSONObject("payload") ?: JSONObject()

            scope.launch {
                when (type) {
                    "new_message" -> {
                        val msgObj = payload.optJSONObject("message") ?: payload
                        _events.emit(WsEvent.NewMessage(msgObj))
                    }
                    "typing" -> {
                        _events.emit(
                            WsEvent.UserTyping(
                                chatId = payload.optString("chatId"),
                                userId = payload.optString("userId"),
                                isTyping = payload.optBoolean("isTyping", true)
                            )
                        )
                    }
                    "presence" -> {
                        _events.emit(
                            WsEvent.UserPresence(
                                userId = payload.optString("userId"),
                                isOnline = payload.optBoolean("isOnline", false)
                            )
                        )
                    }
                    "call_offer" -> {
                        _events.emit(
                            WsEvent.CallOffer(
                                callId = payload.optString("callId"),
                                fromUserId = payload.optString("fromUserId"),
                                chatId = payload.optString("chatId"),
                                isVideo = payload.optBoolean("isVideo", false)
                            )
                        )
                    }
                    "call_ended" -> {
                        _events.emit(WsEvent.CallEnded(payload.optString("callId")))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PrivoWS", "Failed to parse incoming WebSocket message: $text", e)
        }
    }

    fun sendTyping(chatId: String, isTyping: Boolean) {
        val obj = JSONObject().apply {
            put("type", "typing")
            put("payload", JSONObject().apply {
                put("chatId", chatId)
                put("userId", currentUserId)
                put("isTyping", isTyping)
            })
        }
        webSocket?.send(obj.toString())
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "User logged out")
        webSocket = null
    }
}
