package authguidance.mobilesample.plumbing.oauth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.util.Log
import authguidance.mobilesample.configuration.OAuthConfiguration
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class Authenticator(val configuration: OAuthConfiguration, context: Context) {

    private val authStateManager: AuthStateManager

    // For now we remove tokens on every application startup
    init {
        this.authStateManager = AuthStateManager.getInstance(context)
        this.authStateManager.replace(AuthState())
    }

    /*
     * Get the current access token or redirect the user to login
     */
    fun getAccessToken(): String? {

        // Return a token if possible
        val token = this.authStateManager.current.accessToken
        if (!token.isNullOrBlank()) {
            return token
        }

        // Otherwise throw an error that will start the login activity
        val handler = ErrorHandler()
        throw handler.fromLoginRequired()
    }

    /*
     * Do the plumbing to get the authorization intent
     */
    suspend fun startAuthorization(
        activity: Activity,
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
        val authService = AuthorizationService(activity, builder.build())

        // Create and return the custom tabs intent
        val intentBuilder = authService.createCustomTabsIntentBuilder(request.toUri())
        intentBuilder.setToolbarColor(tabHeaderColor);
        val customTabsIntent = intentBuilder.build()

        // Perform the redirect
        authService.performAuthorizationRequest(request, successIntent, failureIntent, customTabsIntent)
    }

    /*
     * When a login redirect completes, process the login response here
     */
    suspend fun finishAuthorization(activity: Activity): Boolean {

        // Get the response details
        val intent = activity.intent
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        // First update auth state
        this.authStateManager.updateAfterAuthorization(authorizationResponse, ex)

        when {
            ex != null -> {
                if(ex.code == 1) {
                    Log.d("GJA", "Authorization redirect cancelled: ${ex.code}, ${ex.type}, ${ex.message}")
                }
                else {
                    Log.d("GJA", "Authorization redirect failed: ${ex.code}, ${ex.type}, ${ex.message}")
                }
            }
            authorizationResponse != null -> {

                // Next swap the authorization code for tokens
                this.exchangeAuthorizationCode(activity, authorizationResponse)

                // Indicate that authorization succeeded
                return true

            }
        }

        // Indicate failure
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
        return suspendCancellableCoroutine { continuation ->

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
    private suspend fun exchangeAuthorizationCode(activity: Activity, authResponse: AuthorizationResponse): Int {

        // Get the PKCE verifier
        val clientAuthentication = authStateManager.current.clientAuthentication

        // Create the AppAuth service object
        val builder = AppAuthConfiguration.Builder()
        builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
        val authService = AuthorizationService(activity, builder.build())

        // Wrap the callback in a coroutine to support cleaner async await based calls
        return suspendCancellableCoroutine { continuation ->

            // Receive the result of the authorization code grant request
            val callback =
                AuthorizationService.TokenResponseCallback { tokenResponse, ex ->

                    // First update the auth state
                    this.authStateManager.updateAfterTokenResponse(tokenResponse, ex)
                    when {
                        // Report errors
                        ex != null -> {
                            Log.d("GJA", "Authorization code grant failed: ${ex.code}, ${ex.type}, ${ex.message}")
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