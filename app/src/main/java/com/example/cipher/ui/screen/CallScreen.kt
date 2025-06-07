package com.example.cipher.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cipher.data.model.Call
import com.example.cipher.data.model.CallStatus
import com.example.cipher.ui.viewmodel.CallViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallScreen(
    callViewModel: CallViewModel,
    currentUsername: String
) {
    val callHistory by callViewModel.callHistory.collectAsStateWithLifecycle()
    val incomingCall by callViewModel.incomingCall.collectAsStateWithLifecycle()
    val currentCall by callViewModel.currentCall.collectAsStateWithLifecycle()
    val isCallActive by callViewModel.isCallActive.collectAsStateWithLifecycle()
    val isLoading by callViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by callViewModel.errorMessage.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main call interface
        if (isCallActive && currentCall != null) {
            ActiveCallScreen(
                call = currentCall!!,
                callViewModel = callViewModel,
                currentUsername = currentUsername
            )
        } else {
            CallHistoryScreen(
                callHistory = callHistory,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRefresh = { callViewModel.loadCallHistory() },
                onClearError = { callViewModel.clearError() }
            )
        }
        
        // Incoming call overlay
        incomingCall?.let { call ->
            IncomingCallOverlay(
                call = call,
                onAnswer = { callViewModel.answerCall(call.id) },
                onDecline = { callViewModel.declineCall(call.id) },
                currentUsername = currentUsername
            )
        }
    }
}

@Composable
fun ActiveCallScreen(
    call: Call,
    callViewModel: CallViewModel,
    currentUsername: String
) {
    val noiseReductionEnabled by callViewModel.noiseReductionEnabled.collectAsStateWithLifecycle()
    val otherParty = if (call.caller == currentUsername) call.callee else call.caller
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = call.status.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Call duration: ${formatDuration(call.startedAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Contact info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = otherParty,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            if (call.noiseReductionEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Noise Reduction Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Call controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Mute button (placeholder)
            FloatingActionButton(
                onClick = { /* TODO: Implement mute */ },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Mute")
            }
            
            // Noise reduction toggle
            FloatingActionButton(
                onClick = { callViewModel.toggleNoiseReduction() },
                containerColor = if (noiseReductionEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (noiseReductionEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = "Noise Reduction")
            }
            
            // Speaker button (placeholder)
            FloatingActionButton(
                onClick = { /* TODO: Implement speaker */ },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speaker")
            }
        }
        
        // End call button
        FloatingActionButton(
            onClick = { callViewModel.endCall(call.id) },
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                Icons.Default.CallEnd,
                contentDescription = "End Call",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun IncomingCallOverlay(
    call: Call,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    currentUsername: String
) {
    val caller = if (call.caller == currentUsername) call.callee else call.caller
    
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Incoming Call",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = caller,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            if (call.noiseReductionEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Noise Reduction Enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Answer/Decline buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Decline button
                FloatingActionButton(
                    onClick = onDecline,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Answer button
                FloatingActionButton(
                    onClick = onAnswer,
                    containerColor = Color.Green,
                    contentColor = Color.White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Answer",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CallHistoryScreen(
    callHistory: List<Call>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Call History",
                style = MaterialTheme.typography.headlineMedium
            )
            
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error message
        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearError) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Call history list
        if (callHistory.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No call history",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(callHistory) { call ->
                    CallHistoryItem(call = call)
                }
            }
        }
    }
}

@Composable
fun CallHistoryItem(call: Call) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Call direction icon
                Icon(
                    when (call.status) {
                        CallStatus.MISSED -> Icons.Default.CallReceived
                        CallStatus.DECLINED -> Icons.Default.CallEnd
                        CallStatus.ENDED -> Icons.Default.Call
                        else -> Icons.Default.Call
                    },
                    contentDescription = null,
                    tint = when (call.status) {
                        CallStatus.MISSED -> MaterialTheme.colorScheme.error
                        CallStatus.DECLINED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = call.callee,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${call.status.name} • ${formatCallTime(call.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (call.noiseReductionEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Noise Reduction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Call duration or status
            call.startedAt?.let { startTime ->
                call.endedAt?.let { endTime ->
                    Text(
                        text = formatCallDuration(startTime, endTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDuration(startedAt: String?): String {
    // This would calculate the duration since the call started
    // For now, returning a placeholder
    return "00:00"
}

private fun formatCallTime(createdAt: String): String {
    return try {
        // This should parse the ISO instant and format it nicely
        // For now, returning a placeholder
        "Today"
    } catch (e: Exception) {
        "Unknown"
    }
}

private fun formatCallDuration(startedAt: String, endedAt: String): String {
    return try {
        // This should calculate the duration between start and end
        // For now, returning a placeholder
        "5:32"
    } catch (e: Exception) {
        "--:--"
    }
}
