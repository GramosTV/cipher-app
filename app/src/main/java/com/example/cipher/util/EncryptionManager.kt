package com.example.cipher.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cipher.data.network.dto.*
import kotlinx.coroutines.flow.first
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

private val Context.encryptionDataStore: DataStore<Preferences> by preferencesDataStore(name = "encryption_prefs")

/**
 * Manages encryption keys and secure messaging for CipherTalk
 */
class EncryptionManager(private val context: Context) {
    
    private val PRIVATE_KEY = stringPreferencesKey("private_key")
    private val PUBLIC_KEY = stringPreferencesKey("public_key")
    private val SESSION_KEYS = stringPreferencesKey("session_keys") // JSON of username -> encrypted session key
    
    private var keyPair: KeyPair? = null
    private val sessionKeys = mutableMapOf<String, SecretKey>()
    
    /**
     * Initialize encryption - generate or load existing key pair
     */
    suspend fun initialize() {
        val storedPrivateKey = context.encryptionDataStore.data.first()[PRIVATE_KEY]
        val storedPublicKey = context.encryptionDataStore.data.first()[PUBLIC_KEY]
        
        if (storedPrivateKey != null && storedPublicKey != null) {
            // Load existing keys
            try {
                val privateKey = CryptoUtils.stringToPrivateKey(storedPrivateKey)
                val publicKey = CryptoUtils.stringToPublicKey(storedPublicKey)
                keyPair = KeyPair(publicKey, privateKey)
            } catch (e: Exception) {
                // If keys are corrupted, generate new ones
                generateAndStoreNewKeyPair()
            }
        } else {
            // Generate new key pair
            generateAndStoreNewKeyPair()
        }
    }
    
    private suspend fun generateAndStoreNewKeyPair() {
        keyPair = CryptoUtils.generateRSAKeyPair()
        
        // Store keys securely
        context.encryptionDataStore.edit { preferences ->
            preferences[PRIVATE_KEY] = CryptoUtils.privateKeyToString(keyPair!!.private)
            preferences[PUBLIC_KEY] = CryptoUtils.publicKeyToString(keyPair!!.public)
        }
    }
    
    /**
     * Get user's public key for sharing
     */
    fun getPublicKey(): String? {
        return keyPair?.public?.let { CryptoUtils.publicKeyToString(it) }
    }
    
    /**
     * Encrypt message for a recipient
     */
    fun encryptMessage(message: String, recipientUsername: String, recipientPublicKey: String): SecureMessageDto? {
        return try {
            val privateKey = keyPair?.private ?: return null
            val publicKey = keyPair?.public ?: return null
            
            // Get or create session key for this recipient
            val sessionKey = sessionKeys[recipientUsername] ?: run {
                val newSessionKey = CryptoUtils.generateAESKey()
                sessionKeys[recipientUsername] = newSessionKey
                newSessionKey
            }
            
            // Encrypt message with session key
            val encryptedMessage = CryptoUtils.encryptMessage(message, sessionKey)
            
            // Encrypt session key with recipient's public key
            val recipientPubKey = CryptoUtils.stringToPublicKey(recipientPublicKey)
            val encryptedSessionKey = CryptoUtils.encryptAESKey(sessionKey, recipientPubKey)
            
            // Sign the message
            val signature = CryptoUtils.signMessage(message, privateKey)
            
            SecureMessageDto(
                encryptedContent = EncryptedMessageDto(
                    ciphertext = encryptedMessage.ciphertext,
                    iv = encryptedMessage.iv
                ),
                encryptedAESKey = encryptedSessionKey,
                signature = signature,
                senderPublicKey = CryptoUtils.publicKeyToString(publicKey),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decrypt received secure message
     */
    fun decryptMessage(secureMessage: SecureMessageDto): String? {
        return try {
            val privateKey = keyPair?.private ?: return null
            
            // Verify signature
            val senderPublicKey = CryptoUtils.stringToPublicKey(secureMessage.senderPublicKey)
            
            // Decrypt session key
            val sessionKey = CryptoUtils.decryptAESKey(secureMessage.encryptedAESKey, privateKey)
            
            // Decrypt message
            val encryptedMessage = EncryptedMessage(
                ciphertext = secureMessage.encryptedContent.ciphertext,
                iv = secureMessage.encryptedContent.iv
            )
            val decryptedMessage = CryptoUtils.decryptMessage(encryptedMessage, sessionKey)
            
            // Verify signature
            val isSignatureValid = CryptoUtils.verifySignature(
                decryptedMessage, 
                secureMessage.signature, 
                senderPublicKey
            )
            
            if (isSignatureValid) {
                // Store session key for this sender
                sessionKeys[secureMessage.sender ?: "unknown"] = sessionKey
                decryptedMessage
            } else {
                null // Invalid signature
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create key exchange message for establishing secure communication
     */
    fun createKeyExchangeMessage(recipientPublicKey: String): KeyExchangeMessageDto? {
        return try {
            val privateKey = keyPair?.private ?: return null
            val publicKey = keyPair?.public ?: return null
            
            // Generate session key
            val sessionKey = CryptoUtils.generateAESKey()
            
            // Encrypt session key with recipient's public key
            val recipientPubKey = CryptoUtils.stringToPublicKey(recipientPublicKey)
            val encryptedSessionKey = CryptoUtils.encryptAESKey(sessionKey, recipientPubKey)
            
            // Sign the session key
            val sessionKeyString = CryptoUtils.secretKeyToString(sessionKey)
            val signature = CryptoUtils.signMessage(sessionKeyString, privateKey)
            
            KeyExchangeMessageDto(
                senderPublicKey = CryptoUtils.publicKeyToString(publicKey),
                encryptedSessionKey = encryptedSessionKey,
                signature = signature,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Process received key exchange message
     */
    fun processKeyExchange(keyExchange: KeyExchangeMessageDto): Boolean {
        return try {
            val privateKey = keyPair?.private ?: return false
            
            // Decrypt session key
            val sessionKey = CryptoUtils.decryptAESKey(keyExchange.encryptedSessionKey, privateKey)
            
            // Verify signature
            val senderPublicKey = CryptoUtils.stringToPublicKey(keyExchange.senderPublicKey)
            val sessionKeyString = CryptoUtils.secretKeyToString(sessionKey)
            val isSignatureValid = CryptoUtils.verifySignature(
                sessionKeyString,
                keyExchange.signature,
                senderPublicKey
            )
            
            if (isSignatureValid) {
                // Store session key for this sender
                sessionKeys[keyExchange.sender ?: "unknown"] = sessionKey
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear all session keys (for logout or security)
     */
    fun clearSessionKeys() {
        sessionKeys.clear()
    }
}
