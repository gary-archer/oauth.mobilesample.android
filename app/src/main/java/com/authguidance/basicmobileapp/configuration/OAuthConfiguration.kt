package com.authguidance.basicmobileapp.configuration

/*
 * OAuth configuration settings
 */
class OAuthConfiguration {
    lateinit var authority: String
    lateinit var clientId: String
    lateinit var redirectUri: String
    lateinit var logoutEndpoint: String
    lateinit var postLogoutRedirectUri: String
    lateinit var scope: String
}