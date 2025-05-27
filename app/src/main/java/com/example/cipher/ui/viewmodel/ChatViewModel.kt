package com.example.cipher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipher.data.network.dto.ChatMessageDto
import com.example.cipher.data.network.websocket.ChatWebSocketService
import com.example.cipher.data.repository.CipherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessageDto> = emptyList(),
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(private val repository: CipherRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        observeWebSocketConnection()
        observeIncomingMessages()
    }
      private fun observeWebSocketConnection() {
        viewModelScope.launch {
            repository.observeWebSocketEvents().collect { event ->
                when (event) {
                    is ChatWebSocketService.WebSocketEvent.OnOpen -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = true,
                            errorMessage = null
                        )
                    }
                    is ChatWebSocketService.WebSocketEvent.OnClosed -> {
                        _uiState.value = _uiState.value.copy(isConnected = false)
                    }
                    is ChatWebSocketService.WebSocketEvent.OnFailure -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = false,
                            errorMessage = "Connection failed: ${event.throwable.message}"
                        )
                    }
                    else -> {
                        // Handle other events if needed
                    }
                }
            }
        }
    }
    
    private fun observeIncomingMessages() {
        viewModelScope.launch {
            repository.observeIncomingMessages().collect { message ->
                message?.let {
                    val currentMessages = _uiState.value.messages.toMutableList()
                    currentMessages.add(it)
                    _uiState.value = _uiState.value.copy(messages = currentMessages)
                }
            }
        }
    }
    
    fun sendMessage(content: String, username: String) {
        if (content.isBlank()) return
        
        val message = ChatMessageDto(
            content = content.trim(),
            sender = username,
            type = "TEXT"
        )
        
        repository.sendMessage(message)
        
        // Add message to local state immediately for better UX
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(message)
        _uiState.value = _uiState.value.copy(messages = currentMessages)
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
