package com.authsamples.basicmobileapp.api.entities

/*
 * A user info entity returned from the API
 */
data class UserInfo(

    // These values originate from OAuth user info
    val givenName: String,
    val familyName: String,

    // This value originates from the API's own data
    val regions: ArrayList<String>
)
