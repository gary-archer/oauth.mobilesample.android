package com.authguidance.basicmobileapp.plumbing.oauth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.lang.IllegalStateException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/*
 * A class to encrypt tokens before storing them and to decrypt tokens when loading
 * This is more complicated than it should be and some notes are available in the below link
 * https://github.com/temyco/security-workshop-sample/blob/master/pages/workshop.md
 */
class EncryptionManager(val context: Context) {

    // Constants
    companion object {
        const val KEYSTORE_NAME = "AndroidKeyStore"
        const val SYMMETRIC_KEY_ALIAS = "encryptionKey"
        const val SYMMETRIC_ALGORITHM = "AES/CBC/PKCS7Padding"
        const val IV_SEPARATOR = "]"
    }

    // Encryption data
    private val keyStore: KeyStore
    private val symmetricKey: SecretKey
    private val cipher: Cipher

    init {

        // Load the keystore
        this.keyStore = KeyStore.getInstance(KEYSTORE_NAME)
        keyStore.load(null)

        // Generate the encryption key the first time the app runs
        // This is a symmetric key only available to our app
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
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(true)

            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
    }

    /*
     * On each construction, load the symmetric key from the key store
     */
    private fun loadSymmetricKeyPair(): SecretKey {

        val privateKey = keyStore.getKey(SYMMETRIC_KEY_ALIAS, null) as SecretKey?
        if (privateKey != null) {
            return privateKey
        }

        throw IllegalStateException("Unable to load the symmetric key named $SYMMETRIC_KEY_ALIAS")
    }

    /*
     * Do the encryption and use the initialisation vector as described here
     * https://proandroiddev.com/secure-data-in-android-initialization-vector-6ca1c659762c
     */
    fun encrypt(plainTextData: String): String {

        // Initialise the cipher
        this.cipher.init(Cipher.ENCRYPT_MODE, this.symmetricKey)

        // Do the encryption
        val encryptedBytes = this.cipher.doFinal(plainTextData.toByteArray())
        val encryptedText = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

        // Use the default initialization vector since caller provided IVs are not yet supported, as described here
        // https://proandroiddev.com/secure-data-in-android-initialization-vector-6ca1c659762c
        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

        // Include the encrypted data and iv, along with a separator outside the base 64 range
        return "$iv$IV_SEPARATOR$encryptedText"
    }

    /*
     * Do the decryption
     */
    fun decrypt(encryptedData: String): String {

        // Split into the expected format
        val parts = encryptedData.split(IV_SEPARATOR.toRegex())
        if (parts.size != 2) {
            throw IllegalStateException("Invalid encrypted data was encountered in EncryptionManager")
        }

        // Get the initialization vector and initialise the cipher
        val iv = parts[0]
        val ivSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
        this.cipher.init(Cipher.DECRYPT_MODE, this.symmetricKey, ivSpec)

        // Do the decryption
        val encryptedPayload = Base64.decode(parts[1], Base64.DEFAULT)
        val decryptedBytes = this.cipher.doFinal(encryptedPayload)

        // Return the plain text
        return String(decryptedBytes)
    }
}
