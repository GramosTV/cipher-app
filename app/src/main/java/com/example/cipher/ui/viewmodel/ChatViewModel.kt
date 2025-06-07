package com.example.cipher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipher.data.network.dto.ChatMessageDto
import com.example.cipher.data.network.dto.UserPublicKeyDto
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
    val errorMessage: String? = null,
    val publicKeys: List<UserPublicKeyDto> = emptyList(),
    val isEncryptionEnabled: Boolean = false
)

class ChatViewModel(private val repository: CipherRepository) : ViewModel() {
      private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        observeWebSocketConnection()
        observeIncomingMessages()
        loadPublicKeys()
        
        // Ensure WebSocket connection is established
        viewModelScope.launch {
            repository.connectToWebSocket()
        }
    }
    
    private fun loadPublicKeys() {
        viewModelScope.launch {
            val keys = repository.getAllPublicKeys()
            _uiState.value = _uiState.value.copy(publicKeys = keys)
        }
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
        
        viewModelScope.launch {
            if (_uiState.value.isEncryptionEnabled) {
                // Send encrypted message to all users with public keys
                val publicKeys = _uiState.value.publicKeys
                for (userKey in publicKeys) {
                    if (userKey.username != username) { // Don't send to self
                        repository.sendSecureMessage(content.trim(), userKey.username, userKey.publicKey)
                    }
                }
            } else {
                // Send regular message
                val message = ChatMessageDto(
                    content = content.trim(),
                    sender = username,
                    type = "TEXT"
                )
                repository.sendMessage(message)
            }
            
            // Add message to local state immediately for better UX
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(ChatMessageDto(
                content = content.trim(),
                sender = username,
                type = "TEXT"
            ))
            _uiState.value = _uiState.value.copy(messages = currentMessages)
        }
    }
    
    fun toggleEncryption() {
        val newEncryptionState = !_uiState.value.isEncryptionEnabled
        _uiState.value = _uiState.value.copy(isEncryptionEnabled = newEncryptionState)
        
        if (newEncryptionState) {
            // Send key exchange messages when enabling encryption
            viewModelScope.launch {
                val publicKeys = _uiState.value.publicKeys
                for (userKey in publicKeys) {
                    repository.sendKeyExchange(userKey.publicKey)
                }
            }
        }
    }
    
    fun getMyPublicKey(): String? {
        return repository.getMyPublicKey()
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
