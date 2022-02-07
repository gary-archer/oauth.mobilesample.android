package com.authsamples.basicmobileapp.plumbing.oauth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.authsamples.basicmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.google.gson.Gson

/*
 * In Android there is no suitable operating system secure storage so we use shared preferences with encryption
 * https://github.com/okta/okta-oidc-android/blob/master/library/src/main/java/com/okta/oidc/storage/security/DefaultEncryptionManager.java
 */
class PersistentTokenStorage(val context: Context, val encryptionManager: EncryptionManager) {

    private var tokenData: TokenData? = null
    private val applicationName = "com.authsamples.basicmobileapp"
    private val key = "AUTH_STATE"
    private val sharedPrefs = context.getSharedPreferences(applicationName, MODE_PRIVATE)

    /*
     * Load tokens from storage and note that the shared preferences API stores them in memory afterwards
     */
    fun loadTokens(): TokenData? {

        if (this.tokenData != null) {
            return this.tokenData
        }

        this.loadTokenData()
        return this.tokenData
    }

    /*
     * Save tokens to stored preferences
     */
    fun saveTokens(newTokenData: TokenData) {

        this.tokenData = newTokenData
        this.saveTokenData()
    }

    /*
     * Remove tokens from storage
     */
    fun removeTokens() {

        this.tokenData = null
        this.sharedPrefs.edit().remove(this.key).apply()
    }

    /*
     * A hacky method for testing, to update token storage to make the access token act like it is expired
     */
    fun expireAccessToken() {

        if (this.tokenData != null) {
            this.tokenData!!.accessToken = "${this.tokenData!!.accessToken}x"
            this.saveTokenData()
        }
    }

    /*
     * A hacky method for testing, to update token storage to make the refresh token act like it is expired
     */
    fun expireRefreshToken() {

        if (this.tokenData != null) {
            this.tokenData!!.accessToken = null
            this.tokenData!!.refreshToken = "${this.tokenData!!.refreshToken}x"
            this.saveTokenData()
        }
    }

    /*
     * Load token data from storage and decrypt
     */
    private fun loadTokenData() {

        if (this.tokenData != null) {
            return
        }

        // See if anything is stored
        val encryptedData = this.sharedPrefs.getString(this.key, "")
        if (encryptedData.isNullOrBlank()) {
            return
        }

        // Decryption errors may be possible if the app is upgraded or re-installed
        try {

            // Try the decrypt operation
            val decryptedData = this.encryptionManager.decrypt(encryptedData)

            // Deserialise the data
            val gson = Gson()
            this.tokenData = gson.fromJson(decryptedData, TokenData::class.java)

        } catch (ex: Throwable) {

            // Swallow this error and return null data, to force a login
            val uiError = ErrorFactory().fromException(ex)
            ErrorConsoleReporter.output(uiError, this.context)
        }
    }

    /*
     * Encrypt tokens and save to stored preferences
     */
    private fun saveTokenData() {
        val gson = Gson()
        val json = gson.toJson(this.tokenData!!)
        val encryptedData = this.encryptionManager.encrypt(json)
        this.sharedPrefs.edit().putString(this.key, encryptedData).apply()
    }
}
