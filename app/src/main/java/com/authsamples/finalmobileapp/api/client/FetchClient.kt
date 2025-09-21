package com.authsamples.finalmobileapp.api.client

import com.authsamples.finalmobileapp.api.entities.ApiUserInfo
import com.authsamples.finalmobileapp.api.entities.Company
import com.authsamples.finalmobileapp.api.entities.CompanyTransactions
import com.authsamples.finalmobileapp.api.entities.OAuthUserInfo
import com.authsamples.finalmobileapp.configuration.Configuration
import com.authsamples.finalmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.finalmobileapp.plumbing.oauth.OAuthClient
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
    private val oauthClient: OAuthClient
) {

    // Create a session id for API logs
    val sessionId = UUID.randomUUID().toString()

    /*
     * Get the list of companies
     */
    suspend fun getCompanyList(options: FetchOptions): Array<Company>? {

        return this.getDataFromApi(
            "${this.configuration.app.apiBaseUrl}/companies",
            Array<Company>::class.java,
            options
        )
    }

    /*
     * Get the list of transactions for a company
     */
    suspend fun getCompanyTransactions(companyId: String, options: FetchOptions): CompanyTransactions? {

        return this.getDataFromApi(
            "${this.configuration.app.apiBaseUrl}/companies/$companyId/transactions",
            CompanyTransactions::class.java,
            options
        )
    }

    /*
     * Download user attributes from the authorization server
     */
    suspend fun getOAuthUserInfo(options: FetchOptions): OAuthUserInfo? {

        return this.getDataFromApi(
            this.configuration.oauth.userInfoEndpoint,
            OAuthUserInfo::class.java,
            options
        )
    }

    /*
     * Download user attributes stored in the API's own data
     */
    suspend fun getApiUserInfo(options: FetchOptions): ApiUserInfo? {

        return this.getDataFromApi(
            "${this.configuration.app.apiBaseUrl}/userinfo",
            ApiUserInfo::class.java,
            options
        )
    }

    /*
     * The entry point for calling an API in a parameterised manner
     */
    private suspend fun <T> getDataFromApi(
        url: String,
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
        if (cacheItem != null && cacheItem.getError() == null) {
            @Suppress("UNCHECKED_CAST")
            return cacheItem.getData() as T?
        }

        // Ensure that the cache item exists for any re-entrant requests that fire after this one
        cacheItem = this.fetchCache.createItem(options.cacheKey)

        try {

            // Get the data and update the cache item for this request
            val data = this.getDataFromApiWithTokenRefresh(url, responseType, options)
            cacheItem.setData(data)
            return data

        } catch (e: Throwable) {

            // Get the error and update the cache item for this request
            val error = ErrorFactory().fromException(e)
            cacheItem.setError(error)
            throw error
        }
    }

    /*
     * A standard algorithm for token refresh
     */
    @Suppress("ThrowsCount")
    private suspend fun <T> getDataFromApiWithTokenRefresh(
        url: String,
        responseType: Class<T>,
        options: FetchOptions
    ): T? {

        // Avoid API requests when there is no access token, and instead trigger a login redirect
        var accessToken = this.oauthClient.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            throw ErrorFactory().fromLoginRequired()
        }

        try {

            // Call the API and return data on success
            return this.callApiWithToken("GET", url, null, accessToken, responseType, options)

        } catch (e1: Throwable) {

            // Report errors if this is not a 401
            val error1 = ErrorFactory().fromException(e1)
            if (error1.statusCode != 401) {
                throw error1
            }

            // Try to refresh the access token
            accessToken = this.oauthClient.synchronizedRefreshAccessToken()

            try {

                // Call the API with the new access token
                return this.callApiWithToken("GET", url, null, accessToken, responseType, options)

            } catch (e2: Throwable) {

                // Save retry errors
                val error2 = ErrorFactory().fromException(e2)
                if (error2.statusCode != 401) {
                    throw error2
                }

                // A permanent API 401 error triggers a new login.
                // This could be caused by an invalid API configuration.
                this.oauthClient.clearLoginState();
                throw ErrorFactory().fromLoginRequired()
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
        builder.header("x-authsamples-api-client", "BasicAndroidApp")
        builder.header("x-authsamples-session-id", this.sessionId)
        builder.header("x-authsamples-correlation-id", UUID.randomUUID().toString())

        // A special header can be sent to the API to cause a simulated exception
        if (options != null && options.causeError) {
            builder.header("x-authsamples-test-exception", "FinalApi")
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
