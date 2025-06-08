package com.authsamples.finalmobileapp.plumbing.oauth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.authsamples.finalmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authsamples.finalmobileapp.plumbing.errors.ErrorFactory
import com.google.gson.Gson

/*
 * Tokens are stored in shared preferences which is not accessible by other apps on the device
 */
class TokenStorage(private val context: Context) {

    private var tokenData: TokenData? = null
    private val applicationName = "com.authsamples.finalmobileapp"
    private val key = "AUTH_STATE"
    private val sharedPrefs = context.getSharedPreferences(applicationName, MODE_PRIVATE)

    /*
     * Load token data from storage on application startup
     */
    fun loadTokens() {

        try {

            // Try the load
            val data = this.sharedPrefs.getString(this.key, "")
            if (data.isNullOrBlank()) {
                return
            }

            // Try to deserialize
            val gson = Gson()
            this.tokenData = gson.fromJson(data, TokenData::class.java)

        } catch (ex: Throwable) {

            // Require a new login if there are problems loading tokens
            val uiError = ErrorFactory().fromException(ex)
            ErrorConsoleReporter.output(uiError, context)
        }
    }

    /*
     * Get tokens if the user has logged in or they have been loaded from storage
     */
    fun getTokens(): TokenData? {
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
     * Save tokens to stored preferences
     */
    private fun saveTokenData() {
        val gson = Gson()
        val data = gson.toJson(this.tokenData!!)
        this.sharedPrefs.edit().putString(this.key, data).apply()
    }
}
