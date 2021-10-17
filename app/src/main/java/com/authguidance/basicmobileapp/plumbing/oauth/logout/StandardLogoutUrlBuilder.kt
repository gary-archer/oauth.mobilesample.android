package com.authguidance.basicmobileapp.plumbing.oauth.logout

import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import net.openid.appauth.AuthorizationServiceConfiguration
import java.net.URLEncoder

/*
 * An implementation of logout handling in line with draft standards
 */
class StandardLogoutUrlBuilder(
    val configuration: OAuthConfiguration,
    val metadata: AuthorizationServiceConfiguration
) : LogoutUrlBuilder {

    /*
     * Build the logout URL from the end session endpoint
     */
    override fun getEndSessionRequestUrl(
        metadata: AuthorizationServiceConfiguration,
        postLogoutRedirectUri: String,
        idTokenHint: String
    ): String {

        val endSessionUrl = this.getEndSessionEndpoint()
        val redirectUri = URLEncoder.encode(postLogoutRedirectUri, "UTF-8")
        val encodedIdToken = URLEncoder.encode(idTokenHint, "UTF-8")
        return "$endSessionUrl?post_logout_redirect_uri=$redirectUri&id_token_hint=$encodedIdToken"
    }

    /*
     * Try to get the end session endpoint from metadata
     */
    private fun getEndSessionEndpoint(): String {

        val key = "end_session_endpoint"
        val json = this.metadata.discoveryDoc!!.docJson
        if (json.has(key)) {
            val result = json.get(key).toString()
            return result
        }

        throw ErrorHandler().fromLogoutNotSupportedError()
    }
}
