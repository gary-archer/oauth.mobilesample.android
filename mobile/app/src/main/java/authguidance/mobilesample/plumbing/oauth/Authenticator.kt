package authguidance.mobilesample.plumbing.oauth

import authguidance.mobilesample.plumbing.utilities.MobileLogger

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

        var logger = MobileLogger();
        logger.debug("Logging the user in to get an access token");
    }
}