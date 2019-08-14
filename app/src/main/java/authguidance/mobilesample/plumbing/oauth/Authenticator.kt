package authguidance.mobilesample.plumbing.oauth

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import authguidance.mobilesample.configuration.OAuthConfiguration
import authguidance.mobilesample.logic.activities.MainActivity
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class Authenticator(val configuration: OAuthConfiguration, val activity: MainActivity) {

    private val authStateManager: AuthStateManager

    // For now we remove tokens on every application startup
    init {
        this.authStateManager = AuthStateManager.getInstance(activity)
        this.authStateManager.replace(AuthState())
    }

    /*
     * Get the current access token or redirect the user to login
     */
    suspend fun getAccessToken(): String? {

        // Return a token if possible
        val token = this.authStateManager.current.accessToken
        if (!token.isNullOrBlank()) {
            println("GJA: IMMEDIATE")
            println("GJA: " + this.authStateManager.current.accessToken);
            return token
        }

        // Otherwise wait for a login, which will take an error action on failure
        this.activity.startLogin()

        // Get the token after a login
        println("GJA: AFTER LOGIN")
        println("GJA: " + this.authStateManager.current.accessToken);
        return this.authStateManager.current.accessToken
    }

    /*
     * Do the plumbing to get the authorization intent
     */
    suspend fun startAuthorization(
        tabHeaderColor: Int,
        successIntent: PendingIntent,
        failureIntent: PendingIntent) {

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
    }

    /*
     * When a login redirect completes, process the login response here
     * Return true on success, false on cancellation, or throw an exception in other cases
     */
    suspend fun finishAuthorization(intent: Intent): Boolean {

        // Get the response details
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        when {
            ex != null -> {
                throw RuntimeException("Authorization response error: ${ex.type} / ${ex.code} / ${ex.message}")
            }
            authorizationResponse != null -> {

                // First update auth state
                this.authStateManager.updateAfterAuthorization(authorizationResponse, ex)

                // Next swap the authorization code for tokens
                this.exchangeAuthorizationCode(authorizationResponse)

                // Indicate that authorization succeeded
                return true
            }
        }

        // Indicate cancellation
        return false
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
     * When a login succeeds, exchange the authorization code for tokens
     */
    private suspend fun exchangeAuthorizationCode(authResponse: AuthorizationResponse): Int {

        // Get the PKCE verifier
        val clientAuthentication = authStateManager.current.clientAuthentication

        // Create the AppAuth service object
        val builder = AppAuthConfiguration.Builder()
        builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
        val authService = AuthorizationService(activity, builder.build())

        // Wrap the callback in a coroutine to support cleaner async await based calls
        return suspendCoroutine { continuation ->

            // Receive the result of the authorization code grant request
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->

                    // First update the auth state
                    this.authStateManager.updateAfterTokenResponse(tokenResponse, ex)
                    when {
                        // Report errors
                        ex != null -> {
                            continuation.resumeWithException(ex)
                        }

                        // Report null responses
                        tokenResponse == null -> {
                            val empty = RuntimeException("Metadata request returned an empty response")
                            continuation.resumeWithException(empty)
                        }

                        // Return token details on success
                        else -> {
                            continuation.resume(0)
                        }
                    }
                }

            // Trigger the token request
            val tokenRequest = authResponse.createTokenExchangeRequest()
            authService.performTokenRequest(
                tokenRequest,
                clientAuthentication,
                callback
            )
        }
    }
}