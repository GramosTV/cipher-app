package com.example.cipher.data.network.dto

import kotlinx.serialization.Serializable

// Request DTOs
@Serializable
data class RegisterRequestDto(
    val username: String,
    val password: String,
    val publicKey: String? = null
)

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

// Response DTOs
@Serializable
data class AuthResponseDto(
    val token: String,
    val username: String,
    val message: String
)

@Serializable
data class UserResponseDto(
    val id: Long,
    val username: String,
    val publicKey: String?
)

// Chat DTOs
@Serializable
data class ChatMessageDto(
    val content: String,
    val sender: String? = null,
    val type: String = "TEXT"
)

// Encrypted message structure for end-to-end encryption
@Serializable
data class EncryptedMessageDto(
    val ciphertext: String,
    val iv: String
)

// Secure message with encryption and signature
@Serializable
data class SecureMessageDto(
    val encryptedContent: EncryptedMessageDto,
    val encryptedAESKey: String, // Encrypted with recipient's public key
    val signature: String,       // Digital signature for integrity
    val senderPublicKey: String, // Sender's public key for verification
    val timestamp: Long,
    val sender: String? = null,
    val type: String = "SECURE"
)

// Key exchange message for establishing secure communication
@Serializable
data class KeyExchangeMessageDto(
    val senderPublicKey: String,
    val encryptedSessionKey: String,
    val signature: String,
    val timestamp: Long,
    val sender: String? = null,
    val type: String = "KEY_EXCHANGE"
)

// User public key response
@Serializable
data class UserPublicKeyDto(
    val username: String,
    val publicKey: String
)

// Contact DTOs
@Serializable
data class ContactRequestDto(
    val targetUsername: String
)

@Serializable
data class ContactResponseDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val status: String,
    val createdAt: String
)

@Serializable
data class ContactActionDto(
    val contactId: Long,
    val action: String // ACCEPT, REJECT, BLOCK, UNBLOCK
)

@Serializable
data class ContactListDto(
    val contacts: List<ContactResponseDto>
)

// Call DTOs
@Serializable
data class InitiateCallDto(
    val calleeUsername: String,
    val noiseReductionEnabled: Boolean = false
)

@Serializable
data class CallResponseDto(
    val id: Long,
    val caller: String,
    val callee: String,
    val status: String,
    val noiseReductionEnabled: Boolean,
    val createdAt: String,
    val startedAt: String? = null,
    val endedAt: String? = null
)

@Serializable
data class CallActionDto(
    val callId: Long,
    val action: String // ANSWER, END, DECLINE
)

@Serializable
data class CallSignalingDto(
    val callId: Long,
    val type: String, // OFFER, ANSWER, ICE_CANDIDATE, CALL_SIGNAL
    val signal: String,
    val sender: String? = null
)

@Serializable
data class ActiveCallsDto(
    val activeCalls: List<CallResponseDto>
)
