package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.configuration.AppConfiguration
import authguidance.mobilesample.plumbing.oauth.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

/*
 * Plumbing related to making HTTP calls
 */
class HttpClient(configuration: AppConfiguration, authenticator: Authenticator) {

    private val authenticator = authenticator
    private val configuration = configuration

    /*
     * The entry point for calling an API in a parameterised manner
     */
    suspend fun callApi(method: String, data: String?, path: String): String? {

        val accessToken = this.authenticator.getAccessToken()

        // Create and configure the client
        val client = OkHttpClient.Builder().build();

        // Create the request
        var body: RequestBody? = null;
        val request = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url("${this.configuration.apiBaseUrl}/$path")
            .build()

        // Receive the response
        this.makeRequest(client, request).use {

            if(it.isSuccessful) {
                return it.body()?.string()
            }
            else {
                var result = "Status: ${it.code()} : ${it.body()?.string()}";
                throw RuntimeException(result)
            }
        }
    }

    /*
     * Make the request and return the response or throw a translated error
     */
    fun makeRequest(client: OkHttpClient, request: Request): Response {

        try {
            return client.newCall(request).execute()
        }
        catch(ex: Throwable) {
            throw ex;
        }
    }
}
