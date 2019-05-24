package authguidance.mobilesample.plumbing.oauth

import android.net.Uri
import android.util.Log
import authguidance.mobilesample.configuration.OAuthConfiguration
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationServiceConfiguration
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
     * Start the redirect to perform the OAuth Authorization request
     */
    suspend fun startLogin() {

        // Get metadata
        val metadata = this.getMetadata() ?: throw RuntimeException("Metadata request returned an empty result")

        // Output results
        Log.d("GJA", metadata.tokenEndpoint.toString())
    }

    /*
     * Get metadata and convert the callback to a suspendable function
     */
    private suspend fun getMetadata(): AuthorizationServiceConfiguration? {

        return suspendCancellableCoroutine { continuation ->

            // Form the metadata URL
            val metadataAddress = "${this.configuration.authority}/.well-known/openid-configuration"
            val metadataUri = Uri.parse(metadataAddress)

            // Define a callback
            val callback =
                AuthorizationServiceConfiguration.RetrieveConfigurationCallback { serviceConfiguration, ex ->
                    if (ex != null) {
                        continuation.resumeWithException(ex)
                    } else {
                        continuation.resumeWith(Result.success(serviceConfiguration))
                    }
                }

            // Start the metadata lookup
            AuthorizationServiceConfiguration.fetchFromUrl(metadataUri, callback, DefaultConnectionBuilder.INSTANCE)
        }
    }
}