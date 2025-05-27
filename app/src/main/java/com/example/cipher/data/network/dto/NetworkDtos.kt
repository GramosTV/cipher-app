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
