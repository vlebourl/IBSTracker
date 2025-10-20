package com.tiarkaerell.ibstracker.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages optional password-based encryption for backups.
 *
 * Uses PBKDF2 to derive encryption keys from user passwords.
 * If no password is set, backups are stored in plaintext (protected only by Google Drive auth).
 */
class EncryptionManager {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_ALGORITHM = "AES"
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 100000 // High iteration count for security
        private const val KEY_LENGTH = 256 // AES-256
        private const val SALT_LENGTH = 16 // 128 bits
        private const val GCM_TAG_LENGTH = 128
        private const val SEPARATOR = "|" // Separates salt, IV, and encrypted data
    }

    /**
     * Derives an encryption key from a password using PBKDF2.
     *
     * @param password The user's password
     * @param salt The salt for key derivation
     * @return The derived secret key
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }

    /**
     * Encrypts the given plaintext with a password.
     *
     * @param plaintext The text to encrypt
     * @param password The password to use for encryption
     * @return Base64-encoded string containing salt, IV, and encrypted data
     */
    fun encrypt(plaintext: String, password: String): String {
        // Generate random salt for this encryption
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        // Derive key from password
        val key = deriveKey(password, salt)

        // Encrypt
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine salt, IV, and encrypted data: SALT|IV|ENCRYPTED_DATA
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        return "$saltBase64$SEPARATOR$ivBase64$SEPARATOR$encryptedBase64"
    }

    /**
     * Decrypts the given encrypted string with a password.
     *
     * @param encryptedData Base64-encoded string containing salt, IV, and encrypted data
     * @param password The password to use for decryption
     * @return The decrypted plaintext
     * @throws IllegalArgumentException if the encrypted data format is invalid
     * @throws javax.crypto.BadPaddingException if the password is incorrect
     */
    fun decrypt(encryptedData: String, password: String): String {
        // Split salt, IV, and encrypted data
        val parts = encryptedData.split(SEPARATOR)
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid encrypted data format")
        }

        val salt = Base64.decode(parts[0], Base64.NO_WRAP)
        val iv = Base64.decode(parts[1], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[2], Base64.NO_WRAP)

        // Derive key from password
        val key = deriveKey(password, salt)

        // Decrypt
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
