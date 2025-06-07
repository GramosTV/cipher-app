package com.example.cipher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cipher.data.repository.CipherRepository
import com.example.cipher.ui.navigation.CipherTalkNavigation
import com.example.cipher.ui.theme.CipherTheme
import com.example.cipher.ui.viewmodel.AuthViewModel
import com.example.cipher.ui.viewmodel.CallViewModel
import com.example.cipher.ui.viewmodel.ChatViewModel
import com.example.cipher.ui.viewmodel.ContactViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: CipherRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var callViewModel: CallViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize repository and ViewModels
        repository = CipherRepository(this)
        authViewModel = AuthViewModel(repository)
        chatViewModel = ChatViewModel(repository)
        contactViewModel = ContactViewModel(repository)
        callViewModel = CallViewModel(repository)
        
        setContent {
            CipherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CipherTalkNavigation(
                        authViewModel = authViewModel,
                        chatViewModel = chatViewModel,
                        contactViewModel = contactViewModel,
                        callViewModel = callViewModel
                    )
                }
            }
        }
    }
}