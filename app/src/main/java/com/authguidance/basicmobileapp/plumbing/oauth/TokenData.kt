package com.authguidance.basicmobileapp.plumbing.oauth

/*
 * Token data that is persisted
 */
data class TokenData(

    // The access token used to call APIs
    var accessToken: String? = null,

    // The refresh token used to renew access tokens
    var refreshToken: String? = null,

    // The id token as proof of authentication, also used for logout
    var idToken: String? = null
)
