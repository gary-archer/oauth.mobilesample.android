package com.authguidance.basicmobileapp.plumbing.oauth

import android.content.Intent

/*
 * An interface to make authentication related operations explicit
 */
interface Authenticator {

    // Try to get an access token
    suspend fun getAccessToken(): String

    // Try to refresh an access token
    suspend fun refreshAccessToken(): String

    // Start a login redirect
    suspend fun startLogin(launchAction: (i: Intent) -> Unit)

    // Complete a login
    suspend fun finishLogin(intent: Intent)

    // For testing, make the access token act expired
    fun expireAccessToken()

    // For testing, make the refresh token act expired
    fun expireRefreshToken()

    // Start a logout redirect
    suspend fun startLogout(launchAction: (i: Intent) -> Unit)

    // Complete a logout
    fun finishLogout()
}
