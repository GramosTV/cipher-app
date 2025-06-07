package com.example.cipher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipher.data.model.Contact
import com.example.cipher.data.model.ContactStatus
import com.example.cipher.data.repository.CipherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel(
    private val repository: CipherRepository
) : ViewModel() {
    
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()
    
    private val _pendingRequests = MutableStateFlow<List<Contact>>(emptyList())
    val pendingRequests: StateFlow<List<Contact>> = _pendingRequests.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadContacts()
        loadPendingRequests()
    }
    
    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.getContacts().fold(
                onSuccess = { contactList ->
                    _contacts.value = contactList.filter { it.status == ContactStatus.ACCEPTED }
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load contacts: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun loadPendingRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.getPendingContactRequests().fold(
                onSuccess = { requestList ->
                    _pendingRequests.value = requestList
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to load pending requests: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun sendContactRequest(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.sendContactRequest(username).fold(
                onSuccess = {
                    loadContacts() // Refresh the lists
                    loadPendingRequests()
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to send contact request: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun acceptContactRequest(contactId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.acceptContactRequest(contactId).fold(
                onSuccess = {
                    loadContacts() // Refresh the lists
                    loadPendingRequests()
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to accept contact request: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun rejectContactRequest(contactId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.rejectContactRequest(contactId).fold(
                onSuccess = {
                    loadPendingRequests() // Refresh pending requests
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to reject contact request: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun blockContact(contactId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.blockContact(contactId).fold(
                onSuccess = {
                    loadContacts() // Refresh contacts
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to block contact: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.deleteContact(contactId).fold(
                onSuccess = {
                    loadContacts() // Refresh contacts
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to delete contact: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun updateContactDisplayName(contactId: Long, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.updateContactDisplayName(contactId, displayName).fold(
                onSuccess = {
                    loadContacts() // Refresh contacts
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to update display name: ${error.message}"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
