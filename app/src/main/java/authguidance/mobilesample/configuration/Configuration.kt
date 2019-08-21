package authguidance.mobilesample.configuration;

import com.google.gson.annotations.SerializedName;

/*
 * A holder for configuration settings
 */
class Configuration {
    @SerializedName("app")
    lateinit var app: AppConfiguration

    @SerializedName("oauth")
    lateinit var oauth: OAuthConfiguration
}
