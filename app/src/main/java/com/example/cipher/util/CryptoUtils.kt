package com.example.cipher.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Comprehensive crypto utilities for end-to-end encryption in CipherTalk
 * Uses RSA for key exchange and AES-GCM for message encryption
 */
object CryptoUtils {
    
    private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val RSA_KEY_SIZE = 2048
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    
    /**
     * Generate RSA key pair for user identity
     */
    fun generateRSAKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(RSA_KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }
    
    /**
     * Generate AES key for session encryption
     */
    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE)
        return keyGenerator.generateKey()
    }
      /**
     * Convert public key to Base64 string for transmission
     */
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Convert private key to Base64 string for storage
     */
    fun privateKeyToString(privateKey: PrivateKey): String {
        return Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Convert Base64 string back to public key
     */
    fun stringToPublicKey(publicKeyString: String): PublicKey {
        val keyBytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }
    
    /**
     * Convert Base64 string back to private key
     */
    fun stringToPrivateKey(privateKeyString: String): PrivateKey {
        val keyBytes = Base64.decode(privateKeyString, Base64.NO_WRAP)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }
    
    /**
     * Convert secret key to Base64 string
     */
    fun secretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
    }
    
    /**
     * Convert Base64 string back to secret key
     */
    fun stringToSecretKey(secretKeyString: String): SecretKey {
        val keyBytes = Base64.decode(secretKeyString, Base64.NO_WRAP)
        return SecretKeySpec(keyBytes, "AES")
    }
    
    /**
     * Encrypt AES key with RSA public key for secure transmission
     */
    fun encryptAESKey(aesKey: SecretKey, recipientPublicKey: PublicKey): String {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
        val encryptedKey = cipher.doFinal(aesKey.encoded)
        return Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt AES key with RSA private key
     */
    fun decryptAESKey(encryptedKeyString: String, privateKey: PrivateKey): SecretKey {
        val encryptedKey = Base64.decode(encryptedKeyString, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedKey = cipher.doFinal(encryptedKey)
        return SecretKeySpec(decryptedKey, "AES")
    }
    
    /**
     * Encrypt message with AES-GCM
     */
    fun encryptMessage(message: String, secretKey: SecretKey): EncryptedMessage {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
        val encryptedBytes = cipher.doFinal(messageBytes)
        
        return EncryptedMessage(
            ciphertext = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }
    
    /**
     * Decrypt message with AES-GCM
     */
    fun decryptMessage(encryptedMessage: EncryptedMessage, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = Base64.decode(encryptedMessage.iv, Base64.NO_WRAP)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        
        val encryptedBytes = Base64.decode(encryptedMessage.ciphertext, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
    
    /**
     * Create digital signature for message integrity
     */
    fun signMessage(message: String, privateKey: PrivateKey): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(message.toByteArray(StandardCharsets.UTF_8))
        val signatureBytes = signature.sign()
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }
    
    /**
     * Verify digital signature
     */
    fun verifySignature(message: String, signatureString: String, publicKey: PublicKey): Boolean {
        return try {
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(message.toByteArray(StandardCharsets.UTF_8))
            val signatureBytes = Base64.decode(signatureString, Base64.NO_WRAP)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate secure random bytes for additional entropy
     */
    fun generateRandomBytes(length: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }
    
    /**
     * Hash password with salt for secure storage
     */
    fun hashPassword(password: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashedPassword = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hashedPassword, Base64.NO_WRAP)
    }
}

/**
 * Data class representing an encrypted message
 */
data class EncryptedMessage(
    val ciphertext: String,
    val iv: String
)

/**
 * Data class representing a complete encrypted chat message
 */
data class SecureMessage(
    val encryptedContent: EncryptedMessage,
    val encryptedAESKey: String, // Encrypted with recipient's public key
    val signature: String,       // Digital signature for integrity
    val senderPublicKey: String, // Sender's public key for verification
    val timestamp: Long
)

/**
 * Key exchange message for establishing secure communication
 */
data class KeyExchangeMessage(
    val senderPublicKey: String,
    val encryptedSessionKey: String,
    val signature: String,
    val timestamp: Long
)
