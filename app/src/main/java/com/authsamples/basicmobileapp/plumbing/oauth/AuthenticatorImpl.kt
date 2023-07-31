package com.authsamples.basicmobileapp.plumbing.oauth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.authsamples.basicmobileapp.configuration.OAuthConfiguration
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.basicmobileapp.plumbing.oauth.logout.CognitoLogoutUrlBuilder
import com.authsamples.basicmobileapp.plumbing.oauth.logout.LogoutUrlBuilder
import com.authsamples.basicmobileapp.plumbing.oauth.logout.StandardLogoutUrlBuilder
import com.authsamples.basicmobileapp.plumbing.utilities.ConcurrentActionHandler
import com.google.gson.JsonParser
import net.openid.appauth.AppAuthConfiguration
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
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.BrowserMatcher
import net.openid.appauth.browser.VersionedBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
@Suppress("TooManyFunctions")
class AuthenticatorImpl(
    private val configuration: OAuthConfiguration,
    private val applicationContext: Context
) : Authenticator {

    private var metadata: AuthorizationServiceConfiguration? = null
    private var loginAuthService: AuthorizationService? = null
    private var logoutAuthService: AuthorizationService? = null
    private val concurrencyHandler = ConcurrentActionHandler()
    private val tokenStorage: PersistentTokenStorage

    /*
     * Create child objects once
     */
    init {
        this.tokenStorage = PersistentTokenStorage(this.applicationContext)
    }

    /*
     * Get metadata and convert the callback to a suspendable function
     */
    override suspend fun getMetadata() {

        // Return if already loaded
        if (this.metadata != null) {
            return
        }

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

                        // Sanity check
                        serviceConfiguration == null -> {
                            val error = RuntimeException("Metadata request returned an empty response")
                            continuation.resumeWithException(error)
                        }

                        // Save metadata on success
                        else -> {
                            this.metadata = serviceConfiguration
                            continuation.resume(Unit)
                        }
                    }
                }

            // Trigger the metadata lookup
            AuthorizationServiceConfiguration.fetchFromUrl(metadataUri, callback, DefaultConnectionBuilder.INSTANCE)
        }
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
        return this.refreshAccessToken()
    }

    /*
     * Try to refresh an access token
     */
    override suspend fun refreshAccessToken(): String {

        val refreshToken = this.tokenStorage.loadTokens()?.refreshToken
        if (!refreshToken.isNullOrBlank()) {

            // Perform token refresh and manage concurrency
            this.concurrencyHandler.execute(this::performRefreshTokenGrant)

            // Return the token on success
            val accessToken = this.tokenStorage.loadTokens()?.accessToken
            if (!accessToken.isNullOrBlank()) {
                return accessToken
            }
        }

        // Otherwise abort the API call via a known exception
        throw ErrorFactory().fromLoginRequired()
    }

    /*
     * Do the work to perform an authorization redirect
     */
    override fun startLogin(launchAction: (i: Intent) -> Unit) {

        try {

            val authService = AuthorizationService(this.applicationContext, this.getBrowserConfiguration())
            this.loginAuthService = authService

            // Create the AppAuth request object and use Authorization Code Flow (PKCE)
            // If required, call builder.setAdditionalParameters to supply details such as acr_values
            val builder = AuthorizationRequest.Builder(
                this.metadata!!,
                this.configuration.clientId,
                ResponseTypeValues.CODE,
                Uri.parse(this.getLoginRedirectUri())
            )
                .setScope(this.configuration.scope)
            val request = builder.build()

            // Do the AppAuth redirect
            val authIntent = authService.getAuthorizationRequestIntent(request)
            launchAction(authIntent)

        } catch (ex: Throwable) {
            throw ErrorFactory().fromLoginOperationError(ex, ErrorCodes.loginRequestFailed)
        }
    }

    /*
     * When a login redirect completes, process the login response here
     */
    override suspend fun finishLogin(intent: Intent) {

        // Get the response details
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        // Free custom tab resources after a login
        // https://github.com/openid/AppAuth-Android/issues/91
        this.loginAuthService?.dispose()
        this.loginAuthService = null

        when {
            ex != null -> {

                // Handle the case where the user closes the Chrome Custom Tab rather than logging in
                if (ex.type == AuthorizationException.TYPE_GENERAL_ERROR &&
                    ex.code == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code
                ) {

                    throw ErrorFactory().fromRedirectCancelled()
                }

                // Translate AppAuth errors to the display format
                throw ErrorFactory().fromLoginOperationError(ex, ErrorCodes.loginResponseFailed)
            }
            authorizationResponse != null -> {

                // Swap the authorization code for tokens and update state
                this.exchangeAuthorizationCode(authorizationResponse)
            }
        }
    }

    /*
     * Remove local state and start the logout redirect to remove the OAuth session cookie
     */
    override fun startLogout(launchAction: (i: Intent) -> Unit) {

        // First force removal of tokens from storage
        val tokens = this.tokenStorage.loadTokens()
        val idToken = tokens?.idToken
        this.tokenStorage.removeTokens()

        try {
            // Fail if there is no id token
            if (idToken == null) {

                val message = "Logout is not possible because tokens have already been removed"
                throw IllegalStateException(message)
            }

            // Create an object to manage logout and use it to form the end session request
            val logoutUrlBuilder = this.createLogoutUrlBuilder()
            val logoutUrl = logoutUrlBuilder.getEndSessionRequestUrl(
                this.metadata!!,
                this.getPostLogoutRedirectUri(),
                idToken
            )

            // Launch the intent to sign the user out
            launchAction(this.getLogoutIntent(logoutUrl))

        } catch (ex: Throwable) {
            throw ErrorFactory().fromLogoutOperationError(ex)
        }
    }

    /*
     * Free resources after a logout
     * https://github.com/openid/AppAuth-Android/issues/91
     */
    override fun finishLogout() {

        if (this.logoutAuthService != null) {
            this.logoutAuthService?.dispose()
            this.logoutAuthService = null
        }
    }

    /*
     * Get data from the OAuth user info endpoint
     */
    override suspend fun getUserInfo(): OAuthUserInfo {

        // First get the current access token
        var accessToken = this.getAccessToken()

        // Get metadata if required
        this.getMetadata()

        // Make the userinfo request
        var response = this.makeUserInfoRequest(accessToken)
        if (response.isSuccessful) {

            // Handle successful responses
            return this.deserializeUserInfo(response)
        } else {

            // Retry once with a new token if there is a 401 error
            if (response.code == 401) {

                // Try to refresh the access token
                accessToken = this.refreshAccessToken()

                // Retry the userinfo request
                response = this.makeUserInfoRequest(accessToken)
                if (response.isSuccessful) {

                    return this.deserializeUserInfo(response)

                } else {

                    // Handle failed responses on the retry
                    throw ErrorFactory().fromHttpResponseError(
                        response,
                        this.configuration.userInfoEndpoint,
                        "authorization server"
                    )
                }
            } else {

                // Handle failed responses on the original call
                throw ErrorFactory().fromHttpResponseError(
                    response,
                    this.configuration.userInfoEndpoint,
                    "authorization server"
                )
            }
        }
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
     * When a login succeeds, exchange the authorization code for tokens
     */
    private suspend fun exchangeAuthorizationCode(authResponse: AuthorizationResponse) {

        // Wrap the request in a coroutine
        return suspendCoroutine { continuation ->

            // Define a callback to handle the result of the authorization code grant
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->

                    when {
                        // Translate AppAuth errors to the display format
                        ex != null -> {
                            val error = ErrorFactory().fromTokenError(ex, ErrorCodes.authorizationCodeGrantFailed)
                            continuation.resumeWithException(error)
                        }

                        // Sanity check
                        tokenResponse == null -> {
                            val empty = RuntimeException("Authorization code grant returned an empty response")
                            continuation.resumeWithException(empty)
                        }

                        // Process the response by saving tokens to secure storage
                        else -> {
                            this.saveTokens(tokenResponse)
                            continuation.resume(Unit)
                        }
                    }
                }

            // Create the authorization code grant request
            val tokenRequest = authResponse.createTokenExchangeRequest()

            // Trigger the request
            val authService = AuthorizationService(this.applicationContext)
            authService.performTokenRequest(tokenRequest, NoClientAuthentication.INSTANCE, callback)
        }
    }

    /*
     * Make a user info request with the current access token
     */
    private suspend fun makeUserInfoRequest(accessToken: String): Response {

        val body: RequestBody? = null
        val builder = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method("GET", body)
            .url(this.configuration.userInfoEndpoint)
        val request = builder.build()

        val client = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        val that = this@AuthenticatorImpl
        return suspendCoroutine { continuation ->

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {

                    continuation.resumeWith(Result.success(response))
                }

                override fun onFailure(call: Call, e: IOException) {

                    val exception = ErrorFactory().fromHttpRequestError(
                        e,
                        that.configuration.userInfoEndpoint,
                        "authorization server"
                    )
                    continuation.resumeWithException(exception)
                }
            })
        }
    }

    /*
     * Deserialize the response data for user info
     */
    private fun deserializeUserInfo(response: Response): OAuthUserInfo {

        if (response.body == null) {
            throw IllegalStateException("Unable to deserialize HTTP response into user info")
        }

        val tree = JsonParser.parseString(response.body?.string())
        val data = tree.asJsonObject
        val givenName = data.get("given_name")?.asString ?: ""
        val familyName = data.get("family_name")?.asString ?: ""
        return OAuthUserInfo(givenName, familyName)
    }

    /*
     * Do the work of refreshing an access token
     */
    private suspend fun performRefreshTokenGrant() {

        // Check we have a refresh token
        val refreshToken = this.tokenStorage.loadTokens()?.refreshToken
        if (refreshToken.isNullOrBlank()) {
            return
        }

        // Get metadata if required
        this.getMetadata()

        // Wrap the request in a coroutine
        return suspendCoroutine { continuation ->

            // Define a callback to handle the result of the refresh token grant
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->

                    when {
                        // Translate AppAuth errors to the display format
                        ex != null -> {

                            // If we get an invalid_grant error it means the refresh token has expired
                            if (ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
                                ex.code == AuthorizationException.TokenRequestErrors.INVALID_GRANT.code
                            ) {
                                // Remove tokens and indicate success, since this is an expected error
                                // The caller will throw a login required error to redirect the user to login again
                                this.tokenStorage.removeTokens()
                                continuation.resume(Unit)

                            } else {

                                // Process real errors
                                val error = ErrorFactory().fromTokenError(ex, ErrorCodes.tokenRenewalError)
                                continuation.resumeWithException(error)
                            }
                        }

                        // Sanity check
                        tokenResponse == null -> {
                            val error = RuntimeException("Refresh token grant returned an empty response")
                            continuation.resumeWithException(error)
                        }

                        // Process the response by saving tokens to secure storage
                        else -> {
                            this.saveTokens(tokenResponse)
                            continuation.resume(Unit)
                        }
                    }
                }

            // Create the refresh token grant request
            val tokenRequest = TokenRequest.Builder(
                this.metadata!!,
                this.configuration.clientId
            )
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
    private fun saveTokens(tokenResponse: TokenResponse) {

        // Create token data from the response
        val tokenData = TokenData()
        tokenData.accessToken = tokenResponse.accessToken
        tokenData.refreshToken = tokenResponse.refreshToken
        tokenData.idToken = tokenResponse.idToken

        // The response may have blank values for these tokens
        if (tokenData.refreshToken.isNullOrBlank() || tokenData.idToken.isNullOrBlank()) {

            // See if there is any existing token data
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

    /*
     * Return the URL to the interstitial page used for login redirects
     * https://mobile.authsamples.com/mobile/postlogin.html
     */
    private fun getLoginRedirectUri(): String {
        return "${this.configuration.webBaseUrl}${this.configuration.loginRedirectPath}"
    }

    /*
     * Return the URL to the interstitial page used for logout redirects
     * https://web.mobile.authsamples.com/mobile/postlogout.html
     */
    private fun getPostLogoutRedirectUri(): String {
        return "${this.configuration.webBaseUrl}${this.configuration.postLogoutRedirectPath}"
    }

    /*
     * Return a builder object depending on which of our 2 providers we are using, which have different implementations
     */
    private fun createLogoutUrlBuilder(): LogoutUrlBuilder {

        return if (this.configuration.authority.lowercase(Locale.ROOT).contains("cognito")) {
            CognitoLogoutUrlBuilder(this.configuration)
        } else {
            StandardLogoutUrlBuilder(this.configuration, this.metadata!!)
        }
    }

    /*
     * Create the intent for the logout redirect
     */
    private fun getLogoutIntent(logoutUrl: String): Intent {

        // Create the auth service and set the browser to use
        val authService = AuthorizationService(this.applicationContext, this.getBrowserConfiguration())
        this.logoutAuthService = authService

        if (this.getBrowser() == VersionedBrowserMatcher.CHROME_CUSTOM_TAB) {

            // Start a logout intent on a Chrome Custom tab
            val customTabsIntent = authService.customTabManager.createTabBuilder().build()
            val logoutIntent = customTabsIntent.intent
            logoutIntent.setPackage(authService.browserDescriptor.packageName)
            logoutIntent.data = Uri.parse(logoutUrl)
            return logoutIntent

        } else {

            // Start a logout intent in the Chrome browser
            val logoutIntent = Intent(Intent.ACTION_VIEW)
            logoutIntent.data = Uri.parse(logoutUrl)
            return logoutIntent
        }
    }

    /*
     * Control the browser to use for login and logout redirects
     */
    private fun getBrowserConfiguration(): AppAuthConfiguration {

        return AppAuthConfiguration.Builder()
            .setBrowserMatcher(BrowserAllowList(this.getBrowser()))
            .build()
    }

    /*
     * Android emulators on levels 31 and 32 do not reliably return to the app after custom tab redirects
     * Work around this bug using the Chrome system browser until these emulator issues stabilize
     */
    private fun getBrowser(): BrowserMatcher {

        if (this.isEmulator() && (Build.VERSION.SDK_INT == 31 || Build.VERSION.SDK_INT == 32)) {
            return VersionedBrowserMatcher.CHROME_BROWSER
        }

        return VersionedBrowserMatcher.CHROME_CUSTOM_TAB
    }

    /*
     * Basic detection on whether the app is running on an emulator
     */
    private fun isEmulator(): Boolean {

        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        return model.contains("emulator") ||
            (model.startsWith("sdk_gphone") && manufacturer.contains("google"))

    }
}
