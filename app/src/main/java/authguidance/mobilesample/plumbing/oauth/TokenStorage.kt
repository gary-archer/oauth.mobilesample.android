package authguidance.mobilesample.plumbing.oauth

/*
 * An abstraction for token storage operations
 */
interface TokenStorage {

    // Load tokens
    fun loadTokens(): TokenData?

    // Save tokens to storage
    fun saveTokens(data: TokenData)

    // Remove tokens from storage
    fun removeTokens()

    // Clear the access token from storage
    fun clearAccessToken()

    // Make the access token act expired
    fun expireAccessToken()

    // Make the refresh token act expired
    fun expireRefreshToken()
}
