package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.configuration.AppConfiguration
import authguidance.mobilesample.plumbing.oauth.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/*
 * Plumbing related to making HTTP calls
 */
class HttpClient(configuration: AppConfiguration, authenticator: Authenticator) {

    private val authenticator = authenticator;
    private val configuration = configuration;

    /*
     * The entry point for calling an API in a parameterised manner
     */
    suspend fun callApi(method: String, path: String): String {

        val accessToken = this.authenticator.getAccessToken()

        // Create and configure the client
        val client = OkHttpClient.Builder().build();

        // Create the request
        val request = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method("GET", null)
            // .url("${this.configuration.apiBaseUrl}/$path")
            .url("https://www.baeldung.com")
            .build()

        // Receive the response
        var response: Response? = null;
        try {
            response = client.newCall(request).execute()
            MobileLogger.debug("Got API response")
            return "Status: ${response.code()}"
        }
        finally {
            response?.close();
        }
    }
}
