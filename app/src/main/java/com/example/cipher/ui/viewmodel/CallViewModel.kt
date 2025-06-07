package com.example.cipher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipher.data.model.Call
import com.example.cipher.data.model.CallStatus
import com.example.cipher.data.network.websocket.ChatWebSocketService
import com.example.cipher.data.repository.CipherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel(
    private val repository: CipherRepository
) : ViewModel() {
    
    private val _activeCalls = MutableStateFlow<List<Call>>(emptyList())
    val activeCalls: StateFlow<List<Call>> = _activeCalls.asStateFlow()
    
    private val _callHistory = MutableStateFlow<List<Call>>(emptyList())
    val callHistory: StateFlow<List<Call>> = _callHistory.asStateFlow()
    
    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _incomingCall = MutableStateFlow<Call?>(null)
    val incomingCall: StateFlow<Call?> = _incomingCall.asStateFlow()
    
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()
    
    private val _noiseReductionEnabled = MutableStateFlow(false)
    val noiseReductionEnabled: StateFlow<Boolean> = _noiseReductionEnabled.asStateFlow()
    
    init {
        loadActiveCalls()
        loadCallHistory()
        observeWebSocketForCalls()
    }
    
    private fun observeWebSocketForCalls() {        viewModelScope.launch {
            repository.observeWebSocketEvents().collect { event ->
                when (event) {
                    is ChatWebSocketService.WebSocketEvent.OnMessage -> {
                        // Handle incoming call signals and notifications
                        handleIncomingCallMessage(event.text)
                    }
                    else -> {}
                }
            }
        }
    }
    
    private suspend fun handleIncomingCallMessage(message: String) {
        // Parse and handle incoming call-related messages
        // This would be implemented based on your WebSocket message format
        // For now, we'll refresh active calls when we receive messages
        loadActiveCalls()
    }
    
    fun loadActiveCalls() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.getActiveCalls().fold(
                onSuccess = { calls ->
                    _activeCalls.value = calls
                    // Check if there's an incoming call
                    val incoming = calls.find { it.status == CallStatus.RINGING }
                    _incomingCall.value = incoming
                    
                    // Check if there's an active call
                    val active = calls.find { it.status == CallStatus.ANSWERED }
                    _currentCall.value = active
                    _isCallActive.value = active != null
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load active calls: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun loadCallHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.getCallHistory().fold(
                onSuccess = { history ->
                    _callHistory.value = history
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load call history: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun initiateCall(calleeUsername: String, noiseReductionEnabled: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.initiateCall(calleeUsername, noiseReductionEnabled).fold(
                onSuccess = { call ->
                    _currentCall.value = call
                    _isCallActive.value = true
                    _noiseReductionEnabled.value = noiseReductionEnabled
                    loadActiveCalls() // Refresh active calls
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to initiate call: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun answerCall(callId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.answerCall(callId).fold(
                onSuccess = { call ->
                    _currentCall.value = call
                    _isCallActive.value = true
                    _incomingCall.value = null
                    _noiseReductionEnabled.value = call.noiseReductionEnabled
                    loadActiveCalls() // Refresh active calls
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to answer call: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun endCall(callId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.endCall(callId).fold(
                onSuccess = { call ->
                    _currentCall.value = null
                    _isCallActive.value = false
                    _incomingCall.value = null
                    _noiseReductionEnabled.value = false
                    loadActiveCalls() // Refresh active calls
                    loadCallHistory() // Refresh call history
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to end call: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun declineCall(callId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.declineCall(callId).fold(
                onSuccess = { call ->
                    _incomingCall.value = null
                    loadActiveCalls() // Refresh active calls
                    loadCallHistory() // Refresh call history
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to decline call: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun toggleNoiseReduction() {
        _noiseReductionEnabled.value = !_noiseReductionEnabled.value
        // Here you would implement the actual noise reduction logic
        // This could involve updating audio processing settings
    }
    
    fun sendCallSignal(callId: Long, type: String, signal: String) {
        viewModelScope.launch {
            repository.sendCallSignal(callId, type, signal).fold(
                onSuccess = {
                    // Signal sent successfully
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to send call signal: ${error.message}"
                }
            )
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun dismissIncomingCall() {
        _incomingCall.value = null
    }
}
