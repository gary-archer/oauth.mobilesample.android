package authguidance.mobilesample.plumbing.oauth

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class Authenticator {

    /*
     * Get the current access token or redirect the user to login
     */
    fun getAccessToken(): String? {

        login();
        return null;
    }

    private fun login() {
        println("*** DEBUG: Logging the user in to get an access token");
    }
}