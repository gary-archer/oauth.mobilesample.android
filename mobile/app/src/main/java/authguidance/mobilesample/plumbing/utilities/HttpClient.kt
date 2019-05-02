package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.configuration.AppConfiguration
import authguidance.mobilesample.plumbing.oauth.Authenticator
import com.google.gson.Gson
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
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
        val client = OkHttpClient.Builder().build()
        val gson = Gson()

        // Configure the request body
        var body: RequestBody? = null
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
     * Use the okhttp library to make an async request for data
     */
    private suspend fun makeRequest(client: OkHttpClient, request: Request): Response {

        return suspendCancellableCoroutine { continuation ->

            client.newCall(request).enqueue(object: Callback {

                override fun onResponse(call: Call?, response: Response) {

                    // Return the response on success
                    continuation.resumeWith(Result.success(response))
                }

                override fun onFailure(call: Call?, e: IOException?) {

                    // Return a translated error on failure
                    var exception =  RuntimeException("Error making HTTP request", e)
                    continuation.resumeWithException(exception)
                }
            });
        }
    }
}
