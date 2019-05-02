package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.configuration.AppConfiguration
import authguidance.mobilesample.plumbing.oauth.Authenticator
import com.google.gson.Gson
import okhttp3.*

/*
 * Plumbing related to making HTTP calls
 */
class HttpClient(configuration: AppConfiguration, authenticator: Authenticator) {

    private val authenticator = authenticator
    private val configuration = configuration

    /*
     * The entry point for calling an API in a parameterised manner
     */
    suspend fun <T> callApi(method: String, path: String, data: Any?, runtimeType: Class<T>): T {

        // First get an access token
        val accessToken = this.authenticator.getAccessToken()

        // Initialise the client
        val client = OkHttpClient.Builder().build();
        val gson = Gson()

        // Configure the request body
        var body: RequestBody? = null;
        if(data != null) {
            body = RequestBody.create(MediaType.get("application/json"), gson.toJson(data))
        }

        // Build the full request
        val request = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url("${this.configuration.apiBaseUrl}/$path")
            .build()

        // Make the request
        this.makeRequest(client, request).use {

            if(it.isSuccessful) {

                // Handle successful responses
                val jsonResponse = it.body()!!.string()
                return gson.fromJson(jsonResponse, runtimeType)
            }
            else {

                // Handle failed responses
                var result = "Status: ${it.code()} : ${it.body()?.string()}";
                throw RuntimeException(result)
            }
        }
    }

    /*
     * Make the request and return the response or throw a translated error
     */
    fun makeRequest(client: OkHttpClient, request: Request): Response {

        // TODO: Make async
        // https://stackoverflow.com/questions/45219379/how-to-make-an-api-request-in-kotlin
        try {
            return client.newCall(request).execute()
        }
        catch(ex: Throwable) {
            throw ex;
        }
    }
}
