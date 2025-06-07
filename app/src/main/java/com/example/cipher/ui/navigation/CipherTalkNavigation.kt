package com.example.cipher.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cipher.ui.screen.CallScreen
import com.example.cipher.ui.screen.ChatScreen
import com.example.cipher.ui.screen.ContactsScreen
import com.example.cipher.ui.screen.LoginScreen
import com.example.cipher.ui.screen.RegisterScreen
import com.example.cipher.ui.viewmodel.AuthViewModel
import com.example.cipher.ui.viewmodel.CallViewModel
import com.example.cipher.ui.viewmodel.ChatViewModel
import com.example.cipher.ui.viewmodel.ContactViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Chat : Screen("chat")
    object Contacts : Screen("contacts")
    object Calls : Screen("calls")
}

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Chat.route, Icons.Default.Chat, "Chat"),
    BottomNavItem(Screen.Contacts.route, Icons.Default.Contacts, "Contacts"),
    BottomNavItem(Screen.Calls.route, Icons.Default.Call, "Calls")
)

@Composable
fun CipherTalkNavigation(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    contactViewModel: ContactViewModel,
    callViewModel: CallViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Check if we should show bottom navigation
    val showBottomNav = authUiState.isLoggedIn && currentRoute in bottomNavItems.map { it.route }
    
    if (authUiState.isLoggedIn) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            // Pop up to the start destination and save state
                                            popUpTo(Screen.Chat.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                MainNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    chatViewModel = chatViewModel,
                    contactViewModel = contactViewModel,
                    callViewModel = callViewModel,
                    startDestination = Screen.Chat.route
                )
            }
        }
    } else {
        MainNavHost(
            navController = navController,
            authViewModel = authViewModel,
            chatViewModel = chatViewModel,
            contactViewModel = contactViewModel,
            callViewModel = callViewModel,
            startDestination = Screen.Login.route
        )
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    contactViewModel: ContactViewModel,
    callViewModel: CallViewModel,
    startDestination: String
) {
    val authUiState by authViewModel.uiState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
          composable(Screen.Chat.route) {
            ChatScreen(
                authViewModel = authViewModel,
                chatViewModel = chatViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Contacts.route) {
            ContactsScreen(
                contactViewModel = contactViewModel,
                onNavigateToCall = { username ->
                    // Initiate a call and navigate to call screen
                    callViewModel.initiateCall(username)
                    navController.navigate(Screen.Calls.route)
                }
            )
        }
        
        composable(Screen.Calls.route) {
            CallScreen(
                callViewModel = callViewModel,
                currentUsername = authUiState.username ?: ""
            )
        }
    }
}
