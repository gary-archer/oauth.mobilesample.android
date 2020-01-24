package com.authguidance.basicmobileapp.plumbing.oauth.logout

import android.app.Activity
import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import net.openid.appauth.AuthorizationService
import java.net.URLEncoder

/*
 * An Okta implementation of logout, which follows draft Open Id Connect standards
 */
class OktaLogoutManager(val configuration: OAuthConfiguration) : LogoutManager {

    // The authorization service manages Chrome Custom Tab resources
    private var logoutAuthService: AuthorizationService? = null

    /*
     * Start a logout redirect
     */
    override fun startLogout(activity: Activity, idTokenHint: String?, completionCode: Int) {

        if (idTokenHint == null) {
            return
        }

        val authService = AuthorizationService(activity)
        this.logoutAuthService = authService

        // Form the standards based URL
        val endSessionUrl = "${this.configuration.authority}/${this.configuration.logoutEndpoint}"
        val postLogoutRedirectUri = URLEncoder.encode(this.configuration.postLogoutRedirectUri, "UTF-8")
        val encodedIdToken = URLEncoder.encode(idTokenHint, "UTF-8")
        val logoutUrl = "$endSessionUrl?post_logout_redirect_uri=$postLogoutRedirectUri&id_token_hint=$encodedIdToken"

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