package com.authsamples.finalmobileapp.api.entities

import com.google.gson.annotations.SerializedName

/*
 * OAuth user info needed by the UI
 */
data class OAuthUserInfo(
    @SerializedName("given_name")
    val givenName: String,

    @SerializedName("family_name")
    val familyName: String
)
