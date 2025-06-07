package com.example.cipher.data.network.api

import com.example.cipher.data.network.dto.*
import retrofit2.http.*

interface CallApiService {
    
    @POST("api/calls/initiate")
    suspend fun initiateCall(@Body request: InitiateCallDto): CallResponseDto
    
    @PUT("api/calls/action")
    suspend fun handleCallAction(@Body action: CallActionDto): CallResponseDto
    
    @GET("api/calls/active")
    suspend fun getActiveCalls(): ActiveCallsDto
    
    @GET("api/calls/history")
    suspend fun getCallHistory(): List<CallResponseDto>
    
    @POST("api/calls/signal")
    suspend fun sendCallSignal(@Body signal: CallSignalingDto)
}
