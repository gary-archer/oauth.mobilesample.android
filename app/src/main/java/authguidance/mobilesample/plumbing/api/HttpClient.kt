package authguidance.mobilesample.plumbing.api

import authguidance.mobilesample.configuration.AppConfiguration
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import authguidance.mobilesample.plumbing.oauth.Authenticator
import com.google.gson.Gson
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import okhttp3.*

/*
 * Plumbing related to making HTTP calls
 */
class HttpClient(configuration: AppConfiguration, authenticator: Authenticator) {

    private val configuration = configuration
    private val authenticator = authenticator

    /*
     * The entry point for calling an API in a parameterised manner
     */
    suspend fun <T> callApi(method: String, path: String, data: Any?, runtimeType: Class<T>): T {

        // Get the full URL
        val url = "${this.configuration.apiBaseUrl}/$path"

        // First get an access token
        var accessToken = this.authenticator.getAccessToken()

        // Make the request
        var response = this.callApiWithToken(method, url, data, accessToken)
        if(response.isSuccessful) {

            // Handle successful responses
            val jsonResponse = response.body()!!.string()
            val gson = Gson()
            return gson.fromJson(jsonResponse, runtimeType)
        }
        else {

            // Retry once with a new token if there is a 401 error
            if(response.code() == 401) {

                // Get a new token
                this.authenticator.clearAccessToken()
                accessToken = this.authenticator.getAccessToken()

                // Retry the API call
                response = this.callApiWithToken(method, url, data, accessToken)
                if(response.isSuccessful) {

                    // Handle successful responses
                    val jsonResponse = response.body()!!.string()
                    val gson = Gson()
                    return gson.fromJson(jsonResponse, runtimeType)
                }
                else {

                    // Handle failed responses on the retry
                    val errorHandler = ErrorHandler()
                    throw errorHandler.fromApiResponseError(response, url)
                }
            } else {

                // Handle failed responses on the original call
                val errorHandler = ErrorHandler()
                throw errorHandler.fromApiResponseError(response, url)
            }
        }
    }

    /*
     * Use the okhttp library to make an async request for data
     */
    private suspend fun callApiWithToken(method: String, url: String, data: Any?, accessToken: String): Response {

        // Configure the request body
        var body: RequestBody? = null
        if(data != null) {
            val gson = Gson()
            body = RequestBody.create(MediaType.get("application/json"), gson.toJson(data))
        }

        // Build the full request
        val request = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url(url)
            .build()

        return suspendCoroutine { continuation ->

            val client = OkHttpClient.Builder().build()
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
