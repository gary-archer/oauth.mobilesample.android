package com.authsamples.basicmobileapp.plumbing.oauth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.authsamples.basicmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.google.gson.Gson

/*
 * Tokens are stored in shared preferences which is not accessible by other apps on the device
 */
class PersistentTokenStorage(val context: Context) {

    private var tokenData: TokenData? = null
    private val applicationName = "com.authsamples.basicmobileapp"
    private val key = "AUTH_STATE"
    private val sharedPrefs = context.getSharedPreferences(applicationName, MODE_PRIVATE)

    /*
     * Load tokens
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
            this.tokenData!!.accessToken = "${this.tokenData!!.accessToken}x"
            this.tokenData!!.refreshToken = "${this.tokenData!!.refreshToken}x"
            this.saveTokenData()
        }
    }

    /*
     * Load token data from storage private to the app
     */
    private fun loadTokenData() {

        if (this.tokenData != null) {
            return
        }

        val data = this.sharedPrefs.getString(this.key, "")
        if (data.isNullOrBlank()) {
            return
        }

        try {

            // Allow recovery from deserialization errors
            val gson = Gson()
            this.tokenData = gson.fromJson(data, TokenData::class.java)

        } catch (ex: Throwable) {

            // Swallow this error and return null data, to force a login
            val uiError = ErrorFactory().fromException(ex)
            ErrorConsoleReporter.output(uiError, this.context)
        }
    }

    /*
     * Save tokens to stored preferences
     */
    private fun saveTokenData() {
        val gson = Gson()
        val data = gson.toJson(this.tokenData!!)
        this.sharedPrefs.edit().putString(this.key, data).apply()
    }
}
