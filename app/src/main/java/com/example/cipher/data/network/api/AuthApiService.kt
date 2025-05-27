package com.example.cipher.data.network.api

import com.example.cipher.data.network.dto.AuthResponseDto
import com.example.cipher.data.network.dto.LoginRequestDto
import com.example.cipher.data.network.dto.RegisterRequestDto
import com.example.cipher.data.network.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("api/auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequestDto): UserResponseDto
    
    @POST("api/auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequestDto): AuthResponseDto
}
