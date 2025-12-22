package com.authsamples.finalmobileapp.configuration

/*
 * OAuth configuration settings
 */
class OAuthConfiguration {

    // The authority base URL
    lateinit var authority: String

    // The identifier for our mobile app
    lateinit var clientId: String

    // The interstitial page that receives the login response
    lateinit var redirectUri: String

    // The interstitial page that receives the logout response
    lateinit var postLogoutRedirectUri: String

    // OAuth scopes being requested, for use when calling APIs after login
    lateinit var scope: String

    // The user info endpoint
    lateinit var userInfoEndpoint: String

    // Some Authorization Servers, such as AWS Cognito, use a custom logout endpoint
    lateinit var customLogoutEndpoint: String

    // The base URL for deep linking
    lateinit var deepLinkBaseUrl: String

    // The name of the ID token claim that contains the ID token
    lateinit var delegationIdClaimName: String
}
