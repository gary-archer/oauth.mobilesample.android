package com.authguidance.basicmobileapp.plumbing.oauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.authguidance.basicmobileapp.configuration.OAuthConfiguration
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.oauth.logout.CognitoLogoutManager
import com.authguidance.basicmobileapp.plumbing.oauth.logout.LogoutManager
import com.authguidance.basicmobileapp.plumbing.oauth.logout.OktaLogoutManager
import com.authguidance.basicmobileapp.plumbing.utilities.ConcurrentActionHandler
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.NoClientAuthentication
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class AuthenticatorImpl(val configuration: OAuthConfiguration, val applicationContext: Context) : Authenticator {

    private val tokenStorage: PersistentTokenStorage
    private val concurrencyHandler: ConcurrentActionHandler
    private val logoutManager: LogoutManager
    private var loginAuthService: AuthorizationService? = null

    /*
     * We will store encrypted tokens in shared preferences, but only if the device is secured
     */
    init {

        // Create an object used to handle refresh token requests from multiple UI frag
        this.concurrencyHandler = ConcurrentActionHandler()

        // Tokens are encrypted and persisted across app restarts
        this.tokenStorage = PersistentTokenStorage(this.applicationContext, EncryptionManager(this.applicationContext))

        // Create an object to manage logout
        this.logoutManager = this.createLogoutManager()
    }

    /*
     * Try to get an access token, which most commonly involves returning the current one
     */
    override suspend fun getAccessToken(): String {

        // See if there is a token in storage
        val accessToken = this.tokenStorage.loadTokens()?.accessToken
        if (!accessToken.isNullOrBlank()) {
            return accessToken
        }

        // Try to use the refresh token to get a new access token
        val newAccessToken = this.refreshAccessToken()
        if (!newAccessToken.isNullOrBlank()) {
            return newAccessToken
        }

        // Otherwise abort the API call via a known exception
        throw ErrorHandler().fromLoginRequired()
    }

    /*
     * Try to refresh an access token
     */
    override suspend fun refreshAccessToken(): String {

        val refreshToken = this.tokenStorage.loadTokens()?.refreshToken
        if (!refreshToken.isNullOrBlank()) {

            // Send the refresh token grant message for the first UI fragment, and synchronise the request
            this.concurrencyHandler.execute(this::performRefreshTokenGrant)

            // Return the token on success
            val accessToken = this.tokenStorage.loadTokens()?.accessToken
            if (!accessToken.isNullOrBlank()) {
                return accessToken
            }
        }

        // Otherwise abort the API call via a known exception
        throw ErrorHandler().fromLoginRequired()
    }

    /*
     * Return true if the user is logged in
     */
    override fun isLoggedIn(): Boolean {
        return this.tokenStorage.loadTokens() != null
    }

    /*
     * Do the work to perform an authorization redirect
     */
    override suspend fun startLogin(activity: Activity, completionCode: Int) {
        this.performLoginRedirect(activity, completionCode)
    }

    /*
     * When a login redirect completes, process the login response here
     */
    override suspend fun finishLogin(intent: Intent) {
        this.handleLoginResponse(intent)
    }

    /*
     * Remove local state and start the logout redirect to remove the OAuth session cookie
     */
    override fun startLogout(activity: Activity, completionCode: Int) {

        // First remove locally stored tokens
        val idToken = this.tokenStorage.loadTokens()?.idToken
        this.tokenStorage.removeTokens()

        // Next do the logout on the system browser
        this.logoutManager.startLogout(activity, idToken, completionCode)
    }

    /*
     * Free resources after a successful logout
     */
    override fun finishLogout() {
        this.logoutManager.finishLogout()
    }

    /*
     * For testing, make the access token act like it is expired
     */
    override fun expireAccessToken() {
        this.tokenStorage.expireAccessToken()
    }

    /*
     * For testing, make the refresh token act like it is expired
     */
    override fun expireRefreshToken() {
        this.tokenStorage.expireRefreshToken()
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
     * Do the work of the login redirect
     */
    private suspend fun performLoginRedirect(activity: Activity, completionCode: Int) {

        val authService = AuthorizationService(activity)
        this.loginAuthService = authService

        // First get metadata
        val metadata = this.getMetadata()

        // Create the AppAuth request object and use the standard mobile value of 'response_type=code'
        val request = AuthorizationRequest.Builder(
            metadata,
            this.configuration.clientId,
            ResponseTypeValues.CODE,
            Uri.parse(this.configuration.redirectUri))
                .setScope(this.configuration.scope)
                .build()

        // Do the AppAuth redirect
        val authIntent = authService.getAuthorizationRequestIntent(request)
        activity.startActivityForResult(authIntent, completionCode)
    }

    /*
     * Handle the response to an authorization redirect
     */
    private suspend fun handleLoginResponse(intent: Intent) {

        // Free custom tab resources
        // https://github.com/openid/AppAuth-Android/issues/91
        this.loginAuthService?.customTabManager?.dispose()
        this.loginAuthService = null

        // Get the response details
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        when {
            ex != null -> {

                // Handle the case where the user closes the Chrome Custom Tab rather than logging in
                if (ex.type == AuthorizationException.TYPE_GENERAL_ERROR &&
                    ex.code == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code) {

                    throw ErrorHandler().fromLoginCancelled()
                }

                // Translate AppAuth errors to the display format
                throw ErrorHandler().fromAppAuthError(ex, ErrorCodes.loginResponseFailed)
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
                    this.handleTokenResponse(tokenResponse, ex, continuation, ErrorCodes.authorizationCodeGrantFailed)
                }

            // Create the authorization code grant request
            val tokenRequest = authResponse.createTokenExchangeRequest()

            // Trigger the request
            val authService = AuthorizationService(this.applicationContext)
            authService.performTokenRequest(tokenRequest, NoClientAuthentication.INSTANCE, callback)
        }
    }

    /*
     * Do the work of refreshing an access token
     */
    private suspend fun performRefreshTokenGrant() {

        // First clear the existing access token from storage
        println("GJA: perform refresh")
        this.tokenStorage.clearAccessToken()

        // Check we have a refresh token
        val refreshToken = this.tokenStorage.loadTokens()?.refreshToken
        if (refreshToken.isNullOrBlank()) {
            return
        }

        // First get metadata
        val metadata = getMetadata()

        // Wrap the request in a coroutine
        return suspendCoroutine { continuation ->

            // Define a callback to handle the result of the refresh token grant
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->

                    if (ex != null &&
                        ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
                        ex.code == AuthorizationException.TokenRequestErrors.INVALID_GRANT.code) {

                        // If we get an invalid_grant error it means the refresh token has expired
                        continuation.resume(Unit)
                    } else {

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

            // Trigger the request
            val authService = AuthorizationService(this.applicationContext)
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
        operationName: String
    ) {

        when {
            ex != null -> {

                // Translate AppAuth errors to the display format
                val error = ErrorHandler().fromAppAuthError(ex, operationName)
                continuation.resumeWithException(error)
            }
            else -> {

                // Update our data
                if (tokenResponse != null) {

                    // Create token data from the response
                    val tokenData = TokenData()
                    tokenData.accessToken = tokenResponse.accessToken
                    tokenData.refreshToken = tokenResponse.refreshToken
                    tokenData.idToken = tokenResponse.idToken

                    // The response may have blank values for these tokens
                    if (tokenData.refreshToken.isNullOrBlank() || tokenData.idToken.isNullOrBlank()) {

                        // See if there is any old data
                        val oldTokenData = this.tokenStorage.loadTokens()
                        if (oldTokenData != null) {

                            // Maintain the existing refresh token unless we received a new 'rolling' refresh token
                            if (tokenData.refreshToken.isNullOrBlank()) {
                                tokenData.refreshToken = oldTokenData.refreshToken
                            }

                            // Maintain the existing id token if required, which may be needed for logout
                            if (tokenData.idToken.isNullOrBlank()) {
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
     * Return the logout manager depending on which of our 2 providers we are using, which have different implementations
     */
    private fun createLogoutManager(): LogoutManager {

        if (this.configuration.authority.toLowerCase(Locale.ROOT).contains("cognito")) {
            return CognitoLogoutManager(this.configuration)
        } else {
            return OktaLogoutManager(this.configuration)
        }
    }
}