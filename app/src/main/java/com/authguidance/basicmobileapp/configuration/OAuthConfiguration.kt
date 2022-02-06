package com.authguidance.basicmobileapp.configuration

/*
 * OAuth configuration settings
 */
class OAuthConfiguration {

    // The authority base URL
    lateinit var authority: String

    // The identifier for our mobile app
    lateinit var clientId: String

    // The base URL for interstitial post login pages
    lateinit var webBaseUrl: String

    // The interstitial page that receives the login response
    lateinit var loginRedirectPath: String

    // The interstitial page that receives the logout response
    lateinit var postLogoutRedirectPath: String

    // OAuth scopes being requested, for use when calling APIs after login
    lateinit var scope: String

    // The base URL for deep linking
    lateinit var deepLinkBaseUrl: String

    // Some Authorization Servers, such as AWS Cognito, use a custom logout endpoint
    lateinit var customLogoutEndpoint: String
}
