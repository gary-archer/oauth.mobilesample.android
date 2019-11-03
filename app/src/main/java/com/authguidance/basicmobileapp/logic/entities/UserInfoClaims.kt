package com.authguidance.basicmobileapp.logic.entities

/*
 * A user info entity returned from the API
 */
data class UserInfoClaims(

    val givenName: String,

    val familyName: String,

    val email: String
)