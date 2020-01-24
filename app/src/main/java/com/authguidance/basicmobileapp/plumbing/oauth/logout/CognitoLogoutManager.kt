package com.authguidance.basicmobileapp.plumbing.oauth.logout

import android.app.Activity
import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import net.openid.appauth.AuthorizationService
import java.net.URLEncoder

/*
 * A Cognito implementation of logout, which is not standards based
 */
class CognitoLogoutManager(val configuration: OAuthConfiguration) : LogoutManager {

    // The authorization service manages Chrome Custom Tab resources
    private var logoutAuthService: AuthorizationService? = null

    /*
     * Start a logout redirect
     */
    override fun startLogout(activity: Activity, idTokenHint: String?, completionCode: Int) {

        val authService = AuthorizationService(activity)
        this.logoutAuthService = authService

        // Format the Cognito specific logout URL
        // https://docs.aws.amazon.com/cognito/latest/developerguide/logout-endpoint.html
        val logoutReturnUrl = URLEncoder.encode(this.configuration.postLogoutRedirectUri, "UTF-8")
        val clientId = URLEncoder.encode(this.configuration.clientId, "UTF-8")
        val logoutUrl = "${this.configuration.logoutEndpoint}?client_id=$clientId&logout_uri=$logoutReturnUrl"

        // Start the logout redirect activity
        val logoutIntent = LogoutHelper().createStartLogoutIntent(logoutUrl, authService)
        activity.startActivityForResult(logoutIntent, completionCode)
    }

    /*
     * Free resources after a logout redirect
     * https://github.com/openid/AppAuth-Android/issues/91
     */
    override fun finishLogout() {

        this.logoutAuthService?.customTabManager?.dispose()
        this.logoutAuthService = null
    }
}