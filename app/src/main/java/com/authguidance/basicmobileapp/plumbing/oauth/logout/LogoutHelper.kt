package com.authguidance.basicmobileapp.plumbing.oauth.logout

import android.content.Intent
import android.net.Uri
import net.openid.appauth.AuthorizationService

/*
 * Logout utilities
 */
class LogoutHelper {

    /*
     * Use the auth service to get a logout request intent for a Chrome Custom Tab
     */
    fun createStartLogoutIntent(logoutUrl: String, authService: AuthorizationService): Intent {

        val customTabsIntent = authService.customTabManager.createTabBuilder().build()
        val logoutIntent = customTabsIntent.intent
        logoutIntent.setPackage(authService.browserDescriptor.packageName)
        logoutIntent.data = Uri.parse(logoutUrl)
        return logoutIntent
    }
}