package com.authsamples.basicmobileapp.api.client

import com.authsamples.basicmobileapp.api.entities.ApiUserInfo
import com.authsamples.basicmobileapp.api.entities.Company
import com.authsamples.basicmobileapp.api.entities.CompanyTransactions
import com.authsamples.basicmobileapp.api.entities.OAuthUserInfo
import com.authsamples.basicmobileapp.configuration.Configuration
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Plumbing related to making HTTP calls
 */
class FetchClient(
    private val configuration: Configuration,
    private val fetchCache: FetchCache,
    private val authenticator: Authenticator
) {

    // Create a session id for API logs
    val sessionId = UUID.randomUUID().toString()

    /*
     * Get the list of companies
     */
    suspend fun getCompanyList(options: FetchOptions): Array<Company>? {

        return this.callApi(
            "${this.configuration.app.apiBaseUrl}/companies",
            "GET",
            null,
            Array<Company>::class.java,
            options
        )
    }

    /*
     * Get the list of transactions for a company
     */
    suspend fun getCompanyTransactions(companyId: String, options: FetchOptions): CompanyTransactions? {

        return this.callApi(
            "${this.configuration.app.apiBaseUrl}/companies/$companyId/transactions",
            "GET",
            null,
            CompanyTransactions::class.java,
            options
        )
    }

    /*
     * Download user attributes from the authorization server
     */
    suspend fun getOAuthUserInfo(options: FetchOptions): OAuthUserInfo? {

        return this.callApi(
            this.configuration.oauth.userInfoEndpoint,
            "GET",
            null,
            OAuthUserInfo::class.java,
            options
        )
    }

    /*
     * Download user attributes stored in the API's own data
     */
    suspend fun getApiUserInfo(options: FetchOptions): ApiUserInfo? {

        return this.callApi(
            "${this.configuration.app.apiBaseUrl}/userinfo",
            "GET",
            null,
            ApiUserInfo::class.java,
            options
        )
    }

    /*
     * The entry point for calling an API in a parameterised manner
     */
    @Suppress("ThrowsCount")
    private suspend fun <T> callApi(
        url: String,
        method: String,
        requestData: Any?,
        responseType: Class<T>,
        options: FetchOptions
    ): T? {

        // Remove the item from the cache when a reload is requested
        if (options.forceReload) {
            this.fetchCache.removeItem(options.cacheKey)
        }

        // Return existing data from the memory cache when available
        // If a view is created whiles its API requests are in flight, this returns null to the view model
        var cacheItem = this.fetchCache.getItem(options.cacheKey)
        if (cacheItem != null) {
            @Suppress("UNCHECKED_CAST")
            return cacheItem.getData() as T?
        }

        // Ensure that the cache item exists, to avoid a redundant API request on every view recreation
        cacheItem = this.fetchCache.createItem(options.cacheKey)

        // Avoid API requests when there is no access token, and instead trigger a login redirect
        var accessToken = this.authenticator.getAccessToken()
        if (accessToken.isNullOrBlank()) {

            val loginRequiredError = ErrorFactory().fromLoginRequired()
            cacheItem.setError(loginRequiredError)
            throw loginRequiredError
        }

        try {

            // Call the API and return data on success
            val data1 = this.callApiWithToken(method, url, requestData, accessToken, responseType, options)
            cacheItem.setData(data1)
            return data1

        } catch (e1: Throwable) {

            val error1 = ErrorFactory().fromException(e1)
            if (error1.statusCode != 401) {

                // Report errors if this is not a 401
                cacheItem.setError(error1)
                throw error1
            }

            try {

                // Try to refresh the access token cookie
                accessToken = this.authenticator.synchronizedRefreshAccessToken()

            } catch (e2: Throwable) {

                // Save refresh errors
                val error2 = ErrorFactory().fromException(e2)
                cacheItem.setError(error2)
                throw error2
            }

            try {

                // Call the API and return data on success
                val data3 = this.callApiWithToken(method, url, requestData, accessToken, responseType, options)
                cacheItem.setData(data3)
                return data3

            } catch (e3: Throwable) {

                // Save retry errors
                val error3 = ErrorFactory().fromException(e3)
                cacheItem.setError(error3)
                throw error3

            }
        }
    }

    /*
     * Use the okhttp library to make an async request for data
     */
    @Suppress("LongParameterList")
    private suspend fun <T> callApiWithToken(
        method: String,
        url: String,
        data: Any?,
        accessToken: String,
        responseType: Class<T>,
        options: FetchOptions? = null
    ): T {

        // Configure the request body
        var body: RequestBody? = null
        if (data != null) {
            val gson = Gson()
            body = gson.toJson(data).toRequestBody("application/json".toMediaType())
        }

        // Build the full request
        val builder = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url(url)

        // Add headers used for API logging
        builder.header("x-mycompany-api-client", "BasicAndroidApp")
        builder.header("x-mycompany-session-id", this.sessionId)
        builder.header("x-mycompany-correlation-id", UUID.randomUUID().toString())

        // A special header can be sent to the API to cause a simulated exception
        if (options != null && options.causeError) {
            builder.header("x-mycompany-test-exception", "SampleApi")
        }

        val request = builder.build()

        // Create an HTTP client
        val client = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        return suspendCoroutine { continuation ->

            client.newCall(request).enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {

                    if (response.isSuccessful) {

                        // Return deserialized data if the call succeeded
                        val responseData = Gson().fromJson(response.body?.string(), responseType)
                        continuation.resumeWith(Result.success(responseData))

                    } else {

                        // Return response errors
                        val responseError = ErrorFactory().fromHttpResponseError(response, url, "API")
                        continuation.resumeWithException(responseError)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {

                    // Return request errors such as connectivity failures
                    val requestError = ErrorFactory().fromHttpRequestError(e, url, "API")
                    continuation.resumeWithException(requestError)
                }
            })
        }
    }
}
