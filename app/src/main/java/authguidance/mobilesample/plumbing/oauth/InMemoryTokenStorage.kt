package authguidance.mobilesample.plumbing.oauth

/*
 * When the device is not secured we store tokens in memory and the user has to login on every app restart
 */
class InMemoryTokenStorage() : TokenStorage {

    private var tokenData: TokenData? = null
    private val lock = Object()

    /*
     * Load tokens from storage and note that the shared preferences API stores them in memory afterwards
     */
    override fun loadTokens(): TokenData? {
        synchronized (lock) {
            return this.tokenData
        }
    }

    /*
     * Save tokens to stored preferences
     */
    override fun saveTokens(data: TokenData) {
        synchronized (lock) {
            this.tokenData = data
        }
    }

    /*
     * Remove tokens from storage
     */
    override fun removeTokens() {
        synchronized (lock) {
            this.tokenData = null
        }
    }

    /*
     * Remove just the access token from storage
     */
    override fun clearAccessToken() {
        synchronized (lock) {
            if(this.tokenData != null) {
                this.tokenData?.accessToken = null
            }
        }
    }

    /*
     * A hacky method for testing, to update token storage to make the access token act like it is expired
     */
    override fun expireAccessToken() {
        synchronized (lock) {
            if(this.tokenData != null) {
                this.tokenData?.accessToken = "x${this.tokenData?.accessToken}x"
            }
        }
    }

    /*
     * A hacky method for testing, to update token storage to make the refresh token act like it is expired
     */
    override fun expireRefreshToken() {
        synchronized (lock) {
            if(this.tokenData != null) {
                this.tokenData?.accessToken = null
                this.tokenData?.refreshToken = "x${this.tokenData?.refreshToken}x"
            }
        }
    }
}