package com.example.cipher.data.network

import android.app.Application
import com.example.cipher.BuildConfig
import com.example.cipher.data.network.api.AuthApiService
import com.example.cipher.data.network.api.UserApiService
import com.example.cipher.data.network.websocket.ChatWebSocketService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    
    // Backend URLs - Update these for production
    private const val HTTP_BASE_URL = "http://10.0.2.2:8080/"
    private const val WS_BASE_URL = "ws://10.0.2.2:8080/ws/chat"
    
    // JSON configuration
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    // Token storage (simple implementation - use DataStore in production)
    private var authToken: String? = null
    
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    private fun createAuthInterceptor() = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = authToken
        
        if (token != null && !originalRequest.url.pathSegments.any { it == "auth" }) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
    
    // OkHttp client
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            
            // Add auth interceptor
            addInterceptor(createAuthInterceptor())
            
            // Add logging interceptor for debug builds
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }
        }.build()
    }
    
    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(HTTP_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
      // API services
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }
      // WebSocket setup
    private var chatWebSocketService: ChatWebSocketService? = null
    
    fun initializeWebSocket(application: Application) {
        if (chatWebSocketService == null) {
            chatWebSocketService = ChatWebSocketService(okHttpClient, WS_BASE_URL)
        }
    }
    
    fun getChatWebSocketService(): ChatWebSocketService {
        return chatWebSocketService
            ?: throw IllegalStateException("WebSocket not initialized. Call initializeWebSocket(application) first.")
    }
}
