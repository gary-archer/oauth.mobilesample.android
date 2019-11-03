package com.authguidance.basicmobileapp.plumbing.oauth

import android.app.Activity
import net.openid.appauth.AuthorizationService

/*
 * A utility to dispose the auth service's Chrome Custom Tab resources after processing a redirect
 * https://github.com/openid/AppAuth-Android/issues/91
 */
class RedirectContext(activity: Activity) {

    val authService: AuthorizationService

    init {
        this.authService = AuthorizationService(activity)
    }

    fun dispose() {
        this.authService.customTabManager.dispose()
    }
}