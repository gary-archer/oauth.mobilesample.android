package authguidance.mobilesample.plumbing.oauth

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import authguidance.mobilesample.configuration.OAuthConfiguration
import authguidance.mobilesample.logic.activities.MainActivity
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class Authenticator(val configuration: OAuthConfiguration, val activity: MainActivity) {

    // Manage token storage
    private var tokenStorage: TokenStorage

    /*
     * We will store encrypted tokens in shared preferences, but only if the device is secured
     */
    init {
        if(this.isDeviceSecured()) {

            // Tokens are encrypted and persisted across app restarts
            this.tokenStorage = PersistentTokenStorage(this.activity, EncryptionManager(this.activity))
        } else {

            // The user has to login on every app restart
            this.tokenStorage = InMemoryTokenStorage()
        }
    }

    /*
     * Get the current access token or redirect the user to login
     */
    suspend fun getAccessToken(): String {

        // See if there is a token in storage
        var accessToken = this.tokenStorage.loadTokens()?.accessToken
        if(!accessToken.isNullOrBlank()) {
            return accessToken
        }

        // Try to use the refresh token to get a new access token
        this.refreshAccessToken()

        // Return the token on success
        accessToken = this.tokenStorage.loadTokens()?.accessToken
        if(!accessToken.isNullOrBlank()) {
            return accessToken
        }

        // Otherwise wait for a login, which will throw an exception on cancellation or failure
        this.activity.startLogin()

        // Return the token on success
        accessToken = this.tokenStorage.loadTokens()?.accessToken
        if(!accessToken.isNullOrBlank()) {
            return accessToken
        }

        // We will never get here, since if startLogin fails or is cancelled an exception is thrown
        throw RuntimeException("getAccessToken failed to return an access token")
    }

    /*
     * Do the work to perform an authorization redirect
     */
    suspend fun startAuthorization(
        successIntent: PendingIntent,
        failureIntent: PendingIntent,
        tabHeaderColor: Int) {

        this.performAuthorizationRedirect(successIntent, failureIntent, tabHeaderColor)
    }

    /*
     * When a login redirect completes, process the login response here
     */
    suspend fun finishAuthorization(intent: Intent) {
        this.handleLoginResponse(intent)
    }

    /*
     * Clear the current access token from storage when an API call fails, to force getting a new one
     */
    fun clearAccessToken() {
        this.tokenStorage.clearAccessToken()
    }

    /*
     * Make the access token act like it is expired
     */
    fun expireAccessToken() {
        this.tokenStorage.expireAccessToken()
    }

    /*
     * Make the refresh token act like it is expired
     */
    fun expireRefreshToken() {
        this.tokenStorage.expireRefreshToken()
    }

    /*
     * Do end session processing, and for now we just remove tokens
     */
    fun logout() {
        this.tokenStorage.removeTokens()
    }

    /*
     * Get metadata and convert the callback to a suspendable function
     */
    private suspend fun getMetadata(): AuthorizationServiceConfiguration {

        // Form the metadata URL
        val metadataAddress = "${this.configuration.authority}/.well-known/openid-configuration"
        val metadataUri = Uri.parse(metadataAddress)

        // Wrap the callback in a coroutine to support cleaner async await based calls
        return suspendCoroutine { continuation ->

            // Receive the result of the metadata request
            val callback =
                AuthorizationServiceConfiguration.RetrieveConfigurationCallback { serviceConfiguration, ex ->
                    when {
                        // Report errors
                        ex != null -> continuation.resumeWithException(ex)

                        // Report null results
                        serviceConfiguration == null -> {
                            val empty = RuntimeException("Metadata request returned an empty response")
                            continuation.resumeWithException(empty)
                        }

                        // Return metadata on success
                        else -> continuation.resumeWith(Result.success(serviceConfiguration))
                    }
                }

            // Trigger the metadata lookup
            AuthorizationServiceConfiguration.fetchFromUrl(metadataUri, callback, DefaultConnectionBuilder.INSTANCE)
        }
    }

    /*
     * Do the work of the authorization redirect
     */
    private suspend fun performAuthorizationRedirect(
        successIntent: PendingIntent,
        failureIntent: PendingIntent,
        tabHeaderColor: Int) {

        // First get metadata
        val metadata = this.getMetadata()

        // Create the AppAuth request object and use the standard mobile value of 'response_type=code'
        val request = AuthorizationRequest.Builder(
            metadata,
            this.configuration.clientId,
            "code",
            Uri.parse(this.configuration.redirectUri)
        ).build()

        // Create the AppAuth service object
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(AnyBrowserMatcher.INSTANCE)
        builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
        val authService = AuthorizationService(this.activity, builder.build())

        // Create and return the custom tabs intent
        val intentBuilder = authService.createCustomTabsIntentBuilder(request.toUri())
        intentBuilder.setToolbarColor(tabHeaderColor);
        val customTabsIntent = intentBuilder.build()

        // Perform the redirect
        authService.performAuthorizationRequest(request, successIntent, failureIntent, customTabsIntent)
        println("GJA: AppAuth redirect issued - waiting")
    }

    /*
     * Handle the response to an authorization redirect
     */
    suspend fun handleLoginResponse(intent: Intent) {

        // Get the response details
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        when {
            ex != null -> {

                // Handle the case where the user closes the Chrome Custom Tab rather than logging in
                if(ex.type == AuthorizationException.TYPE_GENERAL_ERROR &&
                    ex.code == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code) {

                    throw ErrorHandler().fromLoginCancelled()
                }

                // Translate AppAuth errors to the display format
                throw ErrorHandler().fromAppAuthError(ex, "login_response_error")
            }
            authorizationResponse != null -> {

                // Swap the authorization code for tokens
                this.exchangeAuthorizationCode(authorizationResponse)
            }
        }
    }

    /*
     * When a login succeeds, exchange the authorization code for tokens
     */
    private suspend fun exchangeAuthorizationCode(authResponse: AuthorizationResponse) {

        // Wrap the request in a coroutine
        return suspendCoroutine { continuation ->

            // Define a callback to handle the result of the authorization code grant
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->
                    this.handleTokenResponse(tokenResponse, ex, continuation, "authorization_code_grant")
                }

            // Create the authorization code grant request
            val tokenRequest = authResponse.createTokenExchangeRequest()

            // Create the AppAuth service object
            val builder = AppAuthConfiguration.Builder()
            builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
            val authService = AuthorizationService(activity, builder.build())

            // Trigger the request
            authService.performTokenRequest(
                tokenRequest,
                NoClientAuthentication.INSTANCE,
                callback
            )
        }
    }

    /*
     * Try to refresh the access token when it expires
     */
    private suspend fun refreshAccessToken() {

        // Check we have a refresh token
        val refreshToken = this.tokenStorage.loadTokens()?.refreshToken
        if(refreshToken.isNullOrBlank()) {
            return
        }

        // First get metadata
        val metadata = getMetadata()

        // Wrap the request in a coroutine
        return suspendCoroutine { continuation ->

            // Define a callback to handle the result of the refresh token grant
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->

                    if( ex != null &&
                        ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
                        ex.code == AuthorizationException.TokenRequestErrors.INVALID_GRANT.code) {

                        // If we get an invalid_grant error it means the refresh token has expired
                        continuation.resume(Unit)
                    }
                    else {

                        // Handle other responses
                        this.handleTokenResponse(tokenResponse, ex, continuation, "refresh_token")
                    }
                }

            // Create the refresh token grant request
            val tokenRequest = TokenRequest.Builder(
                metadata,
                this.configuration.clientId)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .build()

            // Create the auth service
            val builder = AppAuthConfiguration.Builder()
            builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
            val authService = AuthorizationService(activity, builder.build())

            // Trigger the request
            authService.performTokenRequest(tokenRequest, callback)
        }
    }

    /*
     * Common handling of token responses, for authorization code grant and refresh token messages
     */
    private fun handleTokenResponse(
        tokenResponse: TokenResponse?,
        ex: AuthorizationException?,
        continuation: Continuation<Unit>,
        operationName: String) {

        when {
            ex != null -> {

                // Translate AppAuth errors to the display format
                val error = ErrorHandler().fromAppAuthError(ex, operationName)
                continuation.resumeWithException(error)
            }
            else -> {

                // Update our data
                if(tokenResponse != null) {

                    // Create token data from the response
                    val tokenData = TokenData()
                    tokenData.accessToken = tokenResponse.accessToken
                    tokenData.refreshToken = tokenResponse.refreshToken
                    tokenData.idToken = tokenResponse.idToken

                    // The response may have blank values for these tokens
                    if(tokenData.refreshToken.isNullOrBlank() || tokenData.idToken.isNullOrBlank()) {

                        // See if there is any old data
                        val oldTokenData = this.tokenStorage.loadTokens()
                        if(oldTokenData != null) {

                            // Maintain the existing refresh token unless we received a new 'rolling' refresh token
                            if(tokenData.refreshToken.isNullOrBlank()) {
                                tokenData.refreshToken = oldTokenData.refreshToken
                            }

                            // Maintain the existing id token if required, which may be needed for logout
                            if(tokenData.idToken.isNullOrBlank()) {
                                tokenData.idToken = oldTokenData.idToken
                            }
                        }
                    }

                    this.tokenStorage.saveTokens(tokenData)
                }

                // Resume processing
                continuation.resume(Unit)
            }
        }
    }

    /*
     * See if the device has a PIN, Pattern or Fingerprint enabled
     */
    private fun isDeviceSecured(): Boolean {

        val keyguardManager = this.activity.getSystemService(Context.KEYGUARD_SERVICE)as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return keyguardManager.isDeviceSecure
        }
        else {
            return keyguardManager.isKeyguardSecure
        }
    }
}