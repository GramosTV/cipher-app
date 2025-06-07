package com.example.cipher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipher.data.repository.CipherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val username: String? = null
)

class AuthViewModel(private val repository: CipherRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {        // Check if user is already logged in
        viewModelScope.launch {
            repository.initializeAuth()
            repository.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    val username = repository.getStoredUsername()
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        username = username
                    )
                    // Connect to WebSocket if already logged in
                    repository.connectToWebSocket()
                }
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
              repository.login(username, password)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        username = response.username
                    )
                    // Connect to WebSocket after successful login
                    repository.connectToWebSocket()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed"
                    )
                }
        }
    }
    
    fun register(username: String, password: String, publicKey: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            repository.register(username, password, publicKey)
                .onSuccess {
                    // After successful registration, automatically login
                    login(username, password)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed"
                    )
                }
        }
    }
      fun logout() {
        viewModelScope.launch {
            repository.disconnectFromWebSocket()
            repository.logout()
            _uiState.value = AuthUiState() // Reset to initial state
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
