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
