package authguidance.mobilesample.plumbing.api

import authguidance.mobilesample.configuration.AppConfiguration
import authguidance.mobilesample.plumbing.errors.ErrorHandler
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
        val url = "${this.configuration.apiBaseUrl}/$path"
        val request = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url(url)
            .build()

        // Make the request
        this.makeRequest(client, url, request).use {

            if(it.isSuccessful) {

                // Handle successful responses
                val jsonResponse = it.body()!!.string()
                return gson.fromJson(jsonResponse, runtimeType)
            }
            else {

                // Handle failed responses
                val errorHandler = ErrorHandler()
                throw errorHandler.fromApiResponseError(it, url)
            }
        }
    }

    /*
     * Use the okhttp library to make an async request for data
     */
    private suspend fun makeRequest(client: OkHttpClient, url: String, request: Request): Response {

        return suspendCancellableCoroutine { continuation ->

            client.newCall(request).enqueue(object: Callback {

                override fun onResponse(call: Call?, response: Response) {

                    // Return the data on success
                    continuation.resumeWith(Result.success(response))
                }

                override fun onFailure(call: Call?, e: IOException?) {

                    // Translate the API error
                    val errorHandler = ErrorHandler()
                    val exception = errorHandler.fromApiRequestError(e, url)
                    continuation.resumeWithException(exception)
                }
            })
        }
    }
}
