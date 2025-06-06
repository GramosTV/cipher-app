package com.example.cipher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cipher.data.model.Call
import com.example.cipher.data.model.CallStatus
import com.example.cipher.data.model.Contact
import com.example.cipher.data.model.ContactStatus
import com.example.cipher.data.network.ApiClient
import com.example.cipher.data.network.dto.*
import com.example.cipher.util.EncryptionManager
import com.example.cipher.data.network.dto.UserResponseDto
import com.example.cipher.data.network.websocket.ChatWebSocketService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cipher_prefs")

class CipherRepository(
    private val context: Context,
    private val encryptionManager: EncryptionManager = EncryptionManager(context)
) {
    
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
    fun connectToWebSocket() {
        ApiClient.getChatWebSocketService().connect()
    }
    
    fun disconnectFromWebSocket() {
        ApiClient.getChatWebSocketService().disconnect()
    }
    
    fun observeWebSocketEvents(): Flow<ChatWebSocketService.WebSocketEvent> {
        return ApiClient.getChatWebSocketService().observeWebSocketEvents()
    }
      fun observeIncomingMessages(): Flow<ChatMessageDto?> {
        return ApiClient.getChatWebSocketService().observeIncomingMessages().map { messageJson ->
            try {
                // Try to parse as different message types
                val tempMap = json.decodeFromString<Map<String, Any>>(messageJson)
                val messageType = tempMap["type"] as? String ?: "TEXT"
                
                when (messageType) {
                    "SECURE" -> {
                        // Decrypt secure message
                        val secureMessage = json.decodeFromString<SecureMessageDto>(messageJson)
                        val decryptedContent = encryptionManager.decryptMessage(secureMessage)
                        if (decryptedContent != null) {
                            ChatMessageDto(
                                content = decryptedContent,
                                sender = secureMessage.sender,
                                type = "TEXT"
                            )
                        } else {
                            ChatMessageDto(
                                content = "[Failed to decrypt message]",
                                sender = secureMessage.sender,
                                type = "ERROR"
                            )
                        }
                    }
                    "KEY_EXCHANGE" -> {
                        // Process key exchange
                        val keyExchange = json.decodeFromString<KeyExchangeMessageDto>(messageJson)
                        val success = encryptionManager.processKeyExchange(keyExchange)
                        ChatMessageDto(
                            content = if (success) "[Secure connection established]" else "[Key exchange failed]",
                            sender = keyExchange.sender,
                            type = "SYSTEM"
                        )
                    }
                    else -> {
                        // Regular message
                        json.decodeFromString<ChatMessageDto>(messageJson)
                    }
                }
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
        // Initialize encryption
        encryptionManager.initialize()
    }
    
    // Encryption methods
    suspend fun getUserPublicKey(username: String): String? {
        return try {
            val response = ApiClient.userApiService.getUserPublicKey(username)
            if (response.isSuccessful) {
                response.body()?.publicKey
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAllPublicKeys(): List<UserPublicKeyDto> {
        return try {
            val response = ApiClient.userApiService.getAllPublicKeys()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getMyPublicKey(): String? {
        return encryptionManager.getPublicKey()
    }
    
    fun sendSecureMessage(message: String, recipientUsername: String, recipientPublicKey: String) {
        val secureMessage = encryptionManager.encryptMessage(message, recipientUsername, recipientPublicKey)
        if (secureMessage != null) {
            try {
                val messageJson = json.encodeToString(secureMessage)
                ApiClient.getChatWebSocketService().sendMessage(messageJson)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun sendKeyExchange(recipientPublicKey: String) {
        val keyExchange = encryptionManager.createKeyExchangeMessage(recipientPublicKey)
        if (keyExchange != null) {
            try {
                val messageJson = json.encodeToString(keyExchange)
                ApiClient.getChatWebSocketService().sendMessage(messageJson)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Contact methods
    suspend fun sendContactRequest(targetUsername: String): Result<Contact> {
        return try {
            val request = ContactRequestDto(targetUsername)
            val response = ApiClient.contactApiService.sendContactRequest(request)
            val contact = Contact(
                id = response.id,
                username = response.username,
                displayName = response.displayName,
                status = ContactStatus.valueOf(response.status),
                createdAt = response.createdAt
            )
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
      suspend fun getContacts(): Result<List<Contact>> {
        return try {
            val response = ApiClient.contactApiService.getContacts()
            val contacts = response.contacts.map { dto ->
                Contact(
                    id = dto.id,
                    username = dto.username,
                    displayName = dto.displayName,
                    status = ContactStatus.valueOf(dto.status),
                    createdAt = dto.createdAt
                )
            }
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
      suspend fun getPendingContactRequests(): Result<List<Contact>> {
        return try {
            val response = ApiClient.contactApiService.getPendingRequests()
            val contacts = response.contacts.map { dto ->
                Contact(
                    id = dto.id,
                    username = dto.username,
                    displayName = dto.displayName,
                    status = ContactStatus.valueOf(dto.status),
                    createdAt = dto.createdAt
                )
            }
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun acceptContactRequest(contactId: Long): Result<Contact> {
        return try {
            val action = ContactActionDto(contactId, "ACCEPT")
            val response = ApiClient.contactApiService.handleContactAction(action)
            val contact = Contact(
                id = response.id,
                username = response.username,
                displayName = response.displayName,
                status = ContactStatus.valueOf(response.status),
                createdAt = response.createdAt
            )
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectContactRequest(contactId: Long): Result<Contact> {
        return try {
            val action = ContactActionDto(contactId, "REJECT")
            val response = ApiClient.contactApiService.handleContactAction(action)
            val contact = Contact(
                id = response.id,
                username = response.username,
                displayName = response.displayName,
                status = ContactStatus.valueOf(response.status),
                createdAt = response.createdAt
            )
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun blockContact(contactId: Long): Result<Contact> {
        return try {
            val action = ContactActionDto(contactId, "BLOCK")
            val response = ApiClient.contactApiService.handleContactAction(action)
            val contact = Contact(
                id = response.id,
                username = response.username,
                displayName = response.displayName,
                status = ContactStatus.valueOf(response.status),
                createdAt = response.createdAt
            )
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteContact(contactId: Long): Result<Unit> {
        return try {
            ApiClient.contactApiService.deleteContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateContactDisplayName(contactId: Long, displayName: String): Result<Contact> {
        return try {
            val body = mapOf("displayName" to displayName)
            val response = ApiClient.contactApiService.updateDisplayName(contactId, body)
            val contact = Contact(
                id = response.id,
                username = response.username,
                displayName = response.displayName,
                status = ContactStatus.valueOf(response.status),
                createdAt = response.createdAt
            )
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Call methods
    suspend fun initiateCall(calleeUsername: String, noiseReductionEnabled: Boolean = false): Result<Call> {
        return try {
            val request = InitiateCallDto(calleeUsername, noiseReductionEnabled)
            val response = ApiClient.callApiService.initiateCall(request)
            val call = Call(
                id = response.id,
                caller = response.caller,
                callee = response.callee,
                status = CallStatus.valueOf(response.status),
                noiseReductionEnabled = response.noiseReductionEnabled,
                createdAt = response.createdAt,
                startedAt = response.startedAt,
                endedAt = response.endedAt
            )
            Result.success(call)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun answerCall(callId: Long): Result<Call> {
        return try {
            val action = CallActionDto(callId, "ANSWER")
            val response = ApiClient.callApiService.handleCallAction(action)
            val call = Call(
                id = response.id,
                caller = response.caller,
                callee = response.callee,
                status = CallStatus.valueOf(response.status),
                noiseReductionEnabled = response.noiseReductionEnabled,
                createdAt = response.createdAt,
                startedAt = response.startedAt,
                endedAt = response.endedAt
            )
            Result.success(call)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endCall(callId: Long): Result<Call> {
        return try {
            val action = CallActionDto(callId, "END")
            val response = ApiClient.callApiService.handleCallAction(action)
            val call = Call(
                id = response.id,
                caller = response.caller,
                callee = response.callee,
                status = CallStatus.valueOf(response.status),
                noiseReductionEnabled = response.noiseReductionEnabled,
                createdAt = response.createdAt,
                startedAt = response.startedAt,
                endedAt = response.endedAt
            )
            Result.success(call)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun declineCall(callId: Long): Result<Call> {
        return try {
            val action = CallActionDto(callId, "DECLINE")
            val response = ApiClient.callApiService.handleCallAction(action)
            val call = Call(
                id = response.id,
                caller = response.caller,
                callee = response.callee,
                status = CallStatus.valueOf(response.status),
                noiseReductionEnabled = response.noiseReductionEnabled,
                createdAt = response.createdAt,
                startedAt = response.startedAt,
                endedAt = response.endedAt
            )
            Result.success(call)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
      suspend fun getActiveCalls(): Result<List<Call>> {
        return try {
            val response = ApiClient.callApiService.getActiveCalls()
            val calls = response.activeCalls.map { dto ->
                Call(
                    id = dto.id,
                    caller = dto.caller,
                    callee = dto.callee,
                    status = CallStatus.valueOf(dto.status),
                    noiseReductionEnabled = dto.noiseReductionEnabled,
                    createdAt = dto.createdAt,
                    startedAt = dto.startedAt,
                    endedAt = dto.endedAt
                )
            }
            Result.success(calls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
      suspend fun getCallHistory(): Result<List<Call>> {
        return try {
            val response = ApiClient.callApiService.getCallHistory()
            val calls = response.map { dto ->
                Call(
                    id = dto.id,
                    caller = dto.caller,
                    callee = dto.callee,
                    status = CallStatus.valueOf(dto.status),
                    noiseReductionEnabled = dto.noiseReductionEnabled,
                    createdAt = dto.createdAt,
                    startedAt = dto.startedAt,
                    endedAt = dto.endedAt
                )
            }
            Result.success(calls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendCallSignal(callId: Long, type: String, signal: String): Result<Unit> {
        return try {
            val signalDto = CallSignalingDto(callId, type, signal)
            ApiClient.callApiService.sendCallSignal(signalDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
