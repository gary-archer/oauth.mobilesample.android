package com.authguidance.basicmobileapp.plumbing.oauth

import android.app.Activity
import android.content.Intent

/*
 * An interface to make authentication related operations explicit
 */
interface Authenticator {

    // Try to get an access token
    suspend fun getAccessToken(): String

    // Query the login state
    fun isLoggedIn(): Boolean

    // Start a login redirect
    suspend fun startLogin(activity: Activity, completionCode: Int)

    // Complete a login
    suspend fun finishLogin(intent: Intent)

    // Clear the access token after a 401
    fun clearAccessToken()

    // For testing, make the access token act expired
    fun expireAccessToken()

    // For testing, make the refresh token act expired
    fun expireRefreshToken()

    // Start a logout redirect
    fun startLogout(activity: Activity, completionCode: Int)

    // Complete a logout
    fun finishLogout()
}