package com.authguidance.basicmobileapp.plumbing.oauth.logout

import android.app.Activity

/*
 * An abstraction to allow us to implement logout handling differently for Cognito and Okta
 */
interface LogoutManager {

    // Start a logout redirect
    fun startLogout(activity: Activity, idTokenHint: String?, completionCode: Int)

    // Complete a logout
    fun finishLogout()
}