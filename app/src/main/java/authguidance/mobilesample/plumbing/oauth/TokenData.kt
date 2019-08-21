package authguidance.mobilesample.plumbing.oauth

/*
 * Token data that is persisted
 */
data class TokenData(
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var idToken: String? = null
)
