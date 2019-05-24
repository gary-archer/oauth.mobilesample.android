package authguidance.mobilesample.plumbing.oauth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import authguidance.mobilesample.configuration.OAuthConfiguration
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.resumeWithException

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class Authenticator(val configuration: OAuthConfiguration, context: Context) {

    private val authStateManager: AuthStateManager

    init {
        this.authStateManager = AuthStateManager.getInstance(context)
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

        // TODO: Query auth state for metadata, get if required, then save auth state

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
     * TODO: Update auth state here
     */
    fun finishAuthorization(intent: Intent) {

        val response = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        if (response != null || ex != null) {
            // TODO: Handle both cases
        }

        // TODO: Update auth state
    }

    /*
     * Get metadata and convert the callback to a suspendable function
     */
    private suspend fun getMetadata(): AuthorizationServiceConfiguration {

        return suspendCancellableCoroutine { continuation ->

            // Form the metadata URL
            val metadataAddress = "${this.configuration.authority}/.well-known/openid-configuration"
            val metadataUri = Uri.parse(metadataAddress)

            // Receive the result
            val callback =
                AuthorizationServiceConfiguration.RetrieveConfigurationCallback { serviceConfiguration, ex ->
                    when {
                        // Report errors
                        ex != null -> continuation.resumeWithException(ex)

                        // Report null results
                        serviceConfiguration == null -> {
                            val empty = RuntimeException("Metadata request returned an empty result")
                            continuation.resumeWithException(empty)
                        }

                        // Return metadata on success
                        else -> continuation.resumeWith(Result.success(serviceConfiguration))
                    }
                }

            // Start the metadata lookup
            AuthorizationServiceConfiguration.fetchFromUrl(metadataUri, callback, DefaultConnectionBuilder.INSTANCE)
        }
    }
}