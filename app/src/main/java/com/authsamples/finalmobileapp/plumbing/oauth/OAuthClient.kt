package com.authsamples.finalmobileapp.plumbing.oauth

import android.content.Intent

/*
 * An interface to make authentication related operations explicit
 */
@Suppress("TooManyFunctions")
interface OAuthClient {

    // See if logged in and load tokens
    suspend fun getSession()

    // Try to get an access token
    suspend fun getAccessToken(): String?

    // Try to refresh an access token
    suspend fun synchronizedRefreshAccessToken(): String

    // Return true if there are tokens
    fun isLoggedIn(): Boolean

    // Start a login redirect
    fun startLogin(launchAction: (i: Intent) -> Unit)

    // Complete a login
    suspend fun finishLogin(intent: Intent)

    // Get the delegation ID claim
    fun getDelegationId(): String

    // Start a logout redirect
    fun startLogout(launchAction: (i: Intent) -> Unit)

    // Complete a logout
    fun finishLogout()

    // Allow the app to clear its login state after certain errors
    fun clearLoginState()

    // For testing, make the access token act expired
    fun expireAccessToken()

    // For testing, make the refresh token act expired
    fun expireRefreshToken()
}
