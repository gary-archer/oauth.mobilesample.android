package com.authsamples.basicmobileapp.plumbing.oauth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.lang.IllegalStateException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/*
 * A class to encrypt tokens before storing them and to decrypt tokens when loading
 * AES256-GCM is used, as a modern symmetric authenticated encryption algorithm
 * https://proandroiddev.com/secure-data-in-android-initialization-vector-6ca1c659762c
 */
class EncryptionManager(val context: Context) {

    // Constants
    companion object {
        const val KEYSTORE_NAME = "AndroidKeyStore"
        const val SYMMETRIC_ALGORITHM = "AES/GCM/NoPadding"
        const val SYMMETRIC_KEY_ALIAS = "encryptionKey"
        const val GCM_IV_SIZE = 12
        const val GCM_TAG_SIZE = 16
    }

    // Encryption data
    private val keyStore: KeyStore
    private val symmetricKey: SecretKey
    private val cipher: Cipher

    init {

        // Load the keystore
        this.keyStore = KeyStore.getInstance(KEYSTORE_NAME)
        keyStore.load(null)

        // Generate the encryption key the first time the app runs, to create a key available only to our app
        this.generateAndStoreSymmetricKey()

        // Load our key every time the app runs
        this.symmetricKey = this.loadSymmetricKeyPair()

        // Create the cipher
        this.cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM)
    }

    /*
     * On the first access, generate a symmetric key and store it in the key store
     * By default this will last until Jan 1 2048
     */
    private fun generateAndStoreSymmetricKey() {

        if (!keyStore.containsAlias(SYMMETRIC_KEY_ALIAS)) {

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME)
            val builder = KeyGenParameterSpec.Builder(
                SYMMETRIC_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)

            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
    }

    /*
     * On each construction, load the symmetric key from the key store
     */
    private fun loadSymmetricKeyPair(): SecretKey {

        val key = keyStore.getKey(SYMMETRIC_KEY_ALIAS, null) as SecretKey?
        if (key != null) {
            return key
        }

        throw IllegalStateException("Unable to load the symmetric key named $SYMMETRIC_KEY_ALIAS")
    }

    /*
     * Perform authenticated symmetric encryption
     */
    fun encrypt(plainTextData: String): String {

        // Initialize the cipher, which will create a random IV
        this.cipher.init(Cipher.ENCRYPT_MODE, this.symmetricKey)

        // Do the encryption, which returns the ciphertext and a message authentication code (mac)
        val encryptedBytes = this.cipher.doFinal(plainTextData.toByteArray())
        val encryptedHex = encryptedBytes.toHexString()

        // Use the generated IV since caller provided IVs are not yet supported
        val ivHex = cipher.iv.toHexString()
        return "$ivHex$encryptedHex"
    }

    /*
     * Do the authentication symmetric decryption
     */
    fun decrypt(encryptedHex: String): String {

        // Even the smallest payload should be at least this many bytes, and each is 2 hex characters
        val minSize = (GCM_IV_SIZE + 1 + GCM_TAG_SIZE) * 2
        if (encryptedHex.length < minSize) {
            throw IllegalStateException("The received payload is invalid and cannot be parsed")
        }

        // Get parts of the payload, and the mac is included in the ciphertext
        val ivHex = encryptedHex.substring(0, GCM_IV_SIZE * 2)
        val ivBytes = ivHex.toHexBytes()
        val cipherTextHex = encryptedHex.substring(GCM_IV_SIZE * 2)
        val cipherTextBytes = cipherTextHex.toHexBytes()

        // Initialize the cipher for AES256-GCM decryption
        val parameterSpec = GCMParameterSpec(GCM_TAG_SIZE * 8, ivBytes)
        this.cipher.init(Cipher.DECRYPT_MODE, this.symmetricKey, parameterSpec)

        // Do the decryption and return the plaintext
        val decryptedBytes = this.cipher.doFinal(cipherTextBytes)
        return String(decryptedBytes)
    }

    /*
     * Convert from bytes to hex when encrypting
     */
    fun ByteArray.toHexString(): String {

        return joinToString(separator = "") { eachByte ->
            "%02x".format(eachByte)
        }
    }

    /*
     * Convert from hex characters to bytes when decrypting
     */
    fun String.toHexBytes(): ByteArray {

        return ByteArray(length / 2) { current ->

            val hex = this.substring(current * 2, (current + 1) * 2)
            Integer.parseInt(hex, 16).toByte()
        }
    }
}
