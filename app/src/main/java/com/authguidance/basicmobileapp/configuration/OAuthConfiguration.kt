package com.authguidance.basicmobileapp.configuration

/*
 * OAuth configuration settings
 */
class OAuthConfiguration {

    // The authority base URL
    lateinit var authority: String

    // The identifier for our mobile app
    lateinit var clientId: String

    // The web domain name that hosts the interstitial page
    lateinit var webDomain: String

    // The interstitial page that receives the login response
    lateinit var loginRedirectPath: String

    // The interstitial page that receives the logout response
    lateinit var postLogoutRedirectPath: String

    // The deep link domain, also used to receive claimed HTTPS scheme OAuth responses
    lateinit var deepLinkDomain: String

    // The deep linking path on which the app is invoked, after login
    lateinit var loginActivatePath: String

    // The deep linking path on which the app is invoked, after login
    lateinit var postLogoutActivatePath: String

    // The Authorization Server endpoint used for logouts
    lateinit var logoutEndpoint: String

    // OAuth scopes being requested, for use when calling APIs after login
    lateinit var scope: String
}
