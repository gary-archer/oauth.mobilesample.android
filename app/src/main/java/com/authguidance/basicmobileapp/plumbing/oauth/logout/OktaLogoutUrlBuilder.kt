package com.authguidance.basicmobileapp.plumbing.oauth.logout

import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import net.openid.appauth.AuthorizationServiceConfiguration
import java.net.URLEncoder

/*
 * An Okta implementation of logout handling
 */
class OktaLogoutUrlBuilder(val configuration: OAuthConfiguration) : LogoutUrlBuilder {

    /*
     * Build the Cognito logout URL, which uses standard parameters
     */
    override fun getEndSessionRequestUrl(metadata: AuthorizationServiceConfiguration, idTokenHint: String): String {

        val endSessionUrl = "${this.configuration.authority}/${this.configuration.logoutEndpoint}"
        val postLogoutRedirectUri = URLEncoder.encode(this.configuration.postLogoutRedirectUri, "UTF-8")
        val encodedIdToken = URLEncoder.encode(idTokenHint, "UTF-8")
        return "$endSessionUrl?post_logout_redirect_uri=$postLogoutRedirectUri&id_token_hint=$encodedIdToken"
    }
}