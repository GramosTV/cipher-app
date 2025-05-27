package com.example.cipher.data.network.websocket

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*
import okio.ByteString

class ChatWebSocketService(
    private val okHttpClient: OkHttpClient,
    private val wsUrl: String
) {
    
    private var webSocket: WebSocket? = null
    private val _incomingMessages = Channel<String>(Channel.UNLIMITED)
    private val _webSocketEvents = Channel<WebSocketEvent>(Channel.UNLIMITED)
    
    sealed class WebSocketEvent {
        object OnOpen : WebSocketEvent()
        data class OnMessage(val text: String) : WebSocketEvent()
        data class OnClosing(val code: Int, val reason: String) : WebSocketEvent()
        data class OnClosed(val code: Int, val reason: String) : WebSocketEvent()
        data class OnFailure(val throwable: Throwable, val response: Response?) : WebSocketEvent()
    }
    
    fun observeWebSocketEvents(): Flow<WebSocketEvent> = _webSocketEvents.receiveAsFlow()
    
    fun observeIncomingMessages(): Flow<String> = _incomingMessages.receiveAsFlow()
    
    fun connect() {
        if (webSocket == null) {
            val request = Request.Builder()
                .url(wsUrl)
                .build()
            
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _webSocketEvents.trySend(WebSocketEvent.OnOpen)
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    _incomingMessages.trySend(text)
                    _webSocketEvents.trySend(WebSocketEvent.OnMessage(text))
                }
                
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    val text = bytes.utf8()
                    _incomingMessages.trySend(text)
                    _webSocketEvents.trySend(WebSocketEvent.OnMessage(text))
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    _webSocketEvents.trySend(WebSocketEvent.OnClosing(code, reason))
                }
                
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _webSocketEvents.trySend(WebSocketEvent.OnClosed(code, reason))
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _webSocketEvents.trySend(WebSocketEvent.OnFailure(t, response))
                }
            })
        }
    }
    
    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }
}
