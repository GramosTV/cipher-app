package com.example.cipher.data.network.api

import com.example.cipher.data.network.dto.UserPublicKeyDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {
    
    @GET("api/users/{username}/public-key")
    suspend fun getUserPublicKey(@Path("username") username: String): Response<UserPublicKeyDto>
    
    @GET("api/users/public-keys")
    suspend fun getAllPublicKeys(): Response<List<UserPublicKeyDto>>
}
