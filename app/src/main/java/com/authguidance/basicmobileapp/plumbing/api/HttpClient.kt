package com.authguidance.basicmobileapp.plumbing.api

import com.authguidance.basicmobileapp.configuration.AppConfiguration
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.oauth.Authenticator
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
            return this.readResponseBody(response, runtimeType)
        }
        else {

            // Retry once with a new token if there is a 401 error
            if(response.code == 401) {

                // Get a new token
                this.authenticator.clearAccessToken()
                accessToken = this.authenticator.getAccessToken()

                // Retry the API call
                response = this.callApiWithToken(method, url, data, accessToken)
                if(response.isSuccessful) {

                    // Handle successful responses
                    return this.readResponseBody(response, runtimeType)
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
            body = gson.toJson(data).toRequestBody("application/json".toMediaType())
        }

        // Build the full request
        val request = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url(url)
            .build()

        return suspendCoroutine { continuation ->

            val client = OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .build()

            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {

                    // Return the data on success
                    continuation.resumeWith(Result.success(response))
                }

                override fun onFailure(call: Call, e: IOException) {

                    // Translate the API error
                    val errorHandler = ErrorHandler()
                    val exception = errorHandler.fromApiRequestError(e, url)
                    continuation.resumeWithException(exception)
                }
            })
        }
    }

    /*
     * Read the response if it is safe to do so
     */
    private fun <T> readResponseBody(response: Response, runtimeType: Class<T>): T {

        if(response.body == null) {
            throw RuntimeException("Unable to deserialize API response into type ${runtimeType.simpleName}")
        }

        return Gson().fromJson(response.body?.string(), runtimeType)
    }
}
