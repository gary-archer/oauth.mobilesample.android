package com.authguidance.basicmobileapp.plumbing.oauth.logout

import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import net.openid.appauth.AuthorizationServiceConfiguration
import java.net.URLEncoder

/*
 * A helper class to deal with Cognito specific differences
 */
class CognitoLogoutUrlBuilder(val configuration: OAuthConfiguration) : LogoutUrlBuilder {

    /*
     * Build the Cognito logout URL
     * https://docs.aws.amazon.com/cognito/latest/developerguide/logout-endpoint.html
     */
    override fun getEndSessionRequestUrl(
        metadata: AuthorizationServiceConfiguration,
        postLogoutRedirectUri: String,
        idTokenHint: String
    ): String {

        val logoutReturnUrl = URLEncoder.encode(postLogoutRedirectUri, "UTF-8")
        val clientId = URLEncoder.encode(this.configuration.clientId, "UTF-8")
        return "${this.configuration.customLogoutEndpoint}?client_id=$clientId&logout_uri=$logoutReturnUrl"
    }
}
