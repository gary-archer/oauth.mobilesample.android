package authguidance.mobilesample.plumbing.oauth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import authguidance.mobilesample.configuration.OAuthConfiguration
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.resumeWithException

/*
 * The authenticator class manages integration with the AppAuth libraries
 */
class Authenticator(val configuration: OAuthConfiguration) {

    /*
     * Get the current access token or redirect the user to login
     */
    fun getAccessToken(): String? {

        // Throw an error that will start the login activity
        val handler = ErrorHandler()
        throw handler.fromLoginRequired();
    }

    /*
     * Do the plumbing to get the authorization intent
     */
    suspend fun getAuthorizationIntent(activity: Activity): Intent {

        // First get metadata from the authenticator
        val metadata = this.getMetadata()

        // Create the AppAuth request object
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

        // Create and return the intent
        val intentBuilder = authService.createCustomTabsIntentBuilder(request.toUri())
        val customTabsIntent = intentBuilder.build()
        return authService.getAuthorizationRequestIntent(request, customTabsIntent)
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
                            val ex = RuntimeException("Metadata request returned an empty result")
                            continuation.resumeWithException(ex)
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