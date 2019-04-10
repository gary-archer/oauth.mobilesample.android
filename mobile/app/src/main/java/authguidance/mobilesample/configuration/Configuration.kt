package authguidance.mobilesample.configuration;

import com.google.gson.annotations.SerializedName;

/*
 * A holder for configuration settings
 */
data class Configuration(
    @SerializedName("app") val appConfiguration: AppConfiguration? = null,
    @SerializedName("oauth") val oauthConfiguration: OAuthConfiguration? = null
)
