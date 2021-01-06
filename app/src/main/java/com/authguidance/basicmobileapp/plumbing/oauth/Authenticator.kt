package com.authguidance.basicmobileapp.plumbing.oauth

import android.app.Activity
import android.content.Intent

/*
 * An interface to make authentication related operations explicit
 */
interface Authenticator {

    // Query the login state
    fun isLoggedIn(): Boolean

    // Try to get an access token
    suspend fun getAccessToken(): String

    // Try to refresh an access token
    suspend fun refreshAccessToken(): String

    // Start a login redirect
    suspend fun startLogin(activity: Activity, completionCode: Int)

    // Complete a login
    suspend fun finishLogin(intent: Intent)

    // For testing, make the access token act expired
    fun expireAccessToken()

    // For testing, make the refresh token act expired
    fun expireRefreshToken()

    // Start a logout redirect
    suspend fun startLogout(activity: Activity, completionCode: Int)

    // Complete a logout
    fun finishLogout()
}
