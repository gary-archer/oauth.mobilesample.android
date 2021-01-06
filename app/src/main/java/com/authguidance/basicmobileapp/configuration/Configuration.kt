package com.authguidance.basicmobileapp.configuration

import com.google.gson.annotations.SerializedName

/*
 * A holder for configuration settings
 */
class Configuration {

    // Application properties
    @SerializedName("app")
    lateinit var app: AppConfiguration

    // OAuth plumbing properties
    @SerializedName("oauth")
    lateinit var oauth: OAuthConfiguration
}
