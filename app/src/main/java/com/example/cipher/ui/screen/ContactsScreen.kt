package com.example.cipher.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cipher.data.model.Contact
import com.example.cipher.data.model.ContactStatus
import com.example.cipher.ui.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contactViewModel: ContactViewModel,
    onNavigateToCall: (String) -> Unit
) {
    val contacts by contactViewModel.contacts.collectAsStateWithLifecycle()
    val pendingRequests by contactViewModel.pendingRequests.collectAsStateWithLifecycle()
    val isLoading by contactViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by contactViewModel.errorMessage.collectAsStateWithLifecycle()
    
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showPendingRequestsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contacts",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row {
                // Show pending requests count if any
                if (pendingRequests.isNotEmpty()) {
                    BadgedBox(
                        badge = { Badge { Text("${pendingRequests.size}") } }
                    ) {
                        IconButton(onClick = { showPendingRequestsDialog = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Pending Requests")
                        }
                    }
                } else {
                    IconButton(onClick = { showPendingRequestsDialog = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Pending Requests")
                    }
                }
                
                IconButton(onClick = { showAddContactDialog = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
                }
                
                IconButton(onClick = { contactViewModel.loadContacts() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
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
                    IconButton(onClick = { contactViewModel.clearError() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Contacts list
        if (contacts.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No contacts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add contacts to start chatting and calling",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onCallClick = { onNavigateToCall(contact.username) },
                        onDeleteClick = { contactViewModel.deleteContact(contact.id) },
                        onBlockClick = { contactViewModel.blockContact(contact.id) }
                    )
                }
            }
        }
    }
    
    // Add Contact Dialog
    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onConfirm = { username ->
                contactViewModel.sendContactRequest(username)
                showAddContactDialog = false
            }
        )
    }
    
    // Pending Requests Dialog
    if (showPendingRequestsDialog) {
        PendingRequestsDialog(
            requests = pendingRequests,
            onDismiss = { showPendingRequestsDialog = false },
            onAccept = { contactId -> contactViewModel.acceptContactRequest(contactId) },
            onReject = { contactId -> contactViewModel.rejectContactRequest(contactId) }
        )
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onCallClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBlockClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "@${contact.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onCallClick) {
                    Icon(Icons.Default.Call, contentDescription = "Call")
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Block") },
                            onClick = {
                                onBlockClick()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Contact") },
        text = {
            Column {
                Text("Enter the username of the person you want to add:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(username) },
                enabled = username.isNotBlank()
            ) {
                Text("Send Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PendingRequestsDialog(
    requests: List<Contact>,
    onDismiss: () -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pending Requests") },
        text = {
            LazyColumn {
                if (requests.isEmpty()) {
                    item {
                        Text("No pending requests")
                    }
                } else {
                    items(requests) { request ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = request.displayName,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "@${request.username}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                Row {
                                    IconButton(onClick = { onAccept(request.id) }) {
                                        Icon(Icons.Default.Check, contentDescription = "Accept")
                                    }
                                    IconButton(onClick = { onReject(request.id) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Reject")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
