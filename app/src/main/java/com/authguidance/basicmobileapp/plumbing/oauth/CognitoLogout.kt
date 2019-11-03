package com.authguidance.basicmobileapp.plumbing.oauth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import java.net.URLEncoder

/*
 * A class to manage logout ourselves until support is added to the AppAuth library
 */
class CognitoLogout {

    /*
     * Start the logout redirect and use Cognito's vendor specific solution
     * https://docs.aws.amazon.com/cognito/latest/developerguide/logout-endpoint.html
     */
    fun createStartLogoutIntent(redirectContext: RedirectContext, configuration: OAuthConfiguration): Intent {

        // Form the full logout URL expected by Cognito
        var logoutReturnUrl = URLEncoder.encode(configuration.postLogoutRedirectUri, "UTF-8")
        val clientId = URLEncoder.encode(configuration.clientId, "UTF-8");
        val logoutUrl = "${configuration.logoutEndpoint}?client_id=${clientId}&logout_uri=${logoutReturnUrl}"

        // Get the auth service
        val authService = redirectContext.authService

        // Use it to invoke the logout request on a Chrome Custom Tab
        val customTabsIntent = authService.customTabManager.createTabBuilder().build()
        val logoutIntent = customTabsIntent.intent
        logoutIntent.setPackage(authService.browserDescriptor.packageName)
        logoutIntent.data = Uri.parse(logoutUrl)
        return logoutIntent
    }
}