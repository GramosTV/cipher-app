package com.example.cipher

import android.app.Application
import com.example.cipher.data.network.ApiClient

class CipherTalkApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WebSocket connection
        ApiClient.initializeWebSocket(this)
    }
}
