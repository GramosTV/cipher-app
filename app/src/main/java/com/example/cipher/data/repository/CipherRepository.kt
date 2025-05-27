package com.example.cipher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cipher.data.network.ApiClient
import com.example.cipher.data.network.dto.AuthResponseDto
import com.example.cipher.data.network.dto.ChatMessageDto
import com.example.cipher.data.network.dto.LoginRequestDto
import com.example.cipher.data.network.dto.RegisterRequestDto
import com.example.cipher.data.network.dto.UserResponseDto
import com.example.cipher.data.network.websocket.ChatWebSocketService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cipher_prefs")

class CipherRepository(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }
    
    // Auth methods
    suspend fun register(username: String, password: String, publicKey: String? = null): Result<UserResponseDto> {
        return try {
            val request = RegisterRequestDto(username, password, publicKey)
            val response = ApiClient.authApiService.registerUser(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(username: String, password: String): Result<AuthResponseDto> {
        return try {
            val request = LoginRequestDto(username, password)
            val response = ApiClient.authApiService.loginUser(request)
            
            // Store token and username
            saveAuthData(response.token, response.username)
            ApiClient.setAuthToken(response.token)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        clearAuthData()
        ApiClient.setAuthToken(null)
    }
    
    // Token management
    private suspend fun saveAuthData(token: String, username: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USERNAME_KEY] = username
        }
    }
    
    private suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
        }
    }
    
    suspend fun getStoredToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }
    
    suspend fun getStoredUsername(): String? {
        return context.dataStore.data.first()[USERNAME_KEY]
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] != null
    }
      // Chat methods
    fun observeWebSocketEvents(): Flow<ChatWebSocketService.WebSocketEvent> {
        return ApiClient.getChatWebSocketService().observeWebSocketEvents()
    }
    
    fun observeIncomingMessages(): Flow<ChatMessageDto?> {
        return ApiClient.getChatWebSocketService().observeIncomingMessages().map { messageJson ->
            try {
                json.decodeFromString<ChatMessageDto>(messageJson)
            } catch (e: Exception) {
                null // Invalid JSON
            }
        }
    }
    
    fun sendMessage(message: ChatMessageDto) {
        try {
            val messageJson = json.encodeToString(message)
            ApiClient.getChatWebSocketService().sendMessage(messageJson)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    // Initialize stored token on app start
    suspend fun initializeAuth() {
        val token = getStoredToken()
        if (token != null) {
            ApiClient.setAuthToken(token)
        }
    }
}
