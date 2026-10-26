package com.authsamples.finalmobileapp.api.client

import com.authsamples.finalmobileapp.api.cache.FetchCache
import com.authsamples.finalmobileapp.api.entities.ApiUserInfo
import com.authsamples.finalmobileapp.api.entities.Company
import com.authsamples.finalmobileapp.api.entities.CompanyTransactions
import com.authsamples.finalmobileapp.api.entities.OAuthUserInfo
import com.authsamples.finalmobileapp.configuration.Configuration
import com.authsamples.finalmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.finalmobileapp.plumbing.oauth.OAuthClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * An example API client that implements infrastructure to fetch  and cache sensitive data
 */
class ApiClient(
    private val configuration: Configuration,
    private val oauthClient: OAuthClient,
    private val eventBus: EventBus,
) {

    // Cache API GET responses to reduce API requests
    private val fetchCache = FetchCache()

    // An object to coordinate multiple concurrent API requests
    private val requestCoordinator = ApiRequestCoordinator(eventBus)

    /*
     * Get the list of companies
     */
    suspend fun getCompanyList(options: GetOptions): Array<Company>? {

        return this.getDataFromApi(
            "${this.configuration.app.apiBaseUrl}/companies",
            options,
            Array<Company>::class.java,
        )
    }

    /*
     * Get the list of transactions for a company
     */
    suspend fun getCompanyTransactions(companyId: String, options: GetOptions): CompanyTransactions? {

        return this.getDataFromApi(
            "${this.configuration.app.apiBaseUrl}/companies/$companyId/transactions",
            options,
            CompanyTransactions::class.java,
        )
    }

    /*
     * Download user attributes from the authorization server
     */
    suspend fun getOAuthUserInfo(options: GetOptions): OAuthUserInfo? {

        return this.getDataFromApi(
            this.configuration.oauth.userInfoEndpoint,
            options,
            OAuthUserInfo::class.java,
        )
    }

    /*
     * Download user attributes stored in the API's own data
     */
    suspend fun getApiUserInfo(options: GetOptions): ApiUserInfo? {

        return this.getDataFromApi(
            "${this.configuration.app.apiBaseUrl}/userinfo",
            options,
            ApiUserInfo::class.java,
        )
    }

    /*
     * Return the overall status after a batch of concurrent API requests complete
     */
    fun hasErrors(): Boolean {
        return this.requestCoordinator.hasErrors()
    }

    /*
     * Clear any API state when the user logs out
     */
    fun clearLoginState() {
        this.fetchCache.clearAll()
        this.requestCoordinator.resetState()
    }

    /*
     * The entry point for calling an API in a parameterised manner
     */
    private suspend fun <T> getDataFromApi(
        url: String,
        options: GetOptions,
        responseType: Class<T>,
    ): T? {

        // Remove the item from the cache when a reload is requested
        if (options.forceReload) {
            this.fetchCache.removeItem(options.cacheKey)
        }

        // Return existing data from the memory cache when available
        var cacheItem = this.fetchCache.getItem(options.cacheKey)
        if (cacheItem != null) {
            @Suppress("UNCHECKED_CAST")
            return cacheItem.getData() as T?
        }

        // Create the cache item with a null value, so that any additional view requests return null
        cacheItem = this.fetchCache.createItem(options.cacheKey)

        // Notify the request coordinator on the main thread that we are sending an API request
        val that = this@ApiClient
        withContext(Dispatchers.Main) {
            that.requestCoordinator.onApiRequestStart()
        }

        try {

            // Get the data and update the cache item for this request
            val data = this.callApiWithTokenRefresh("GET", url, null, options.causeError, responseType)
            cacheItem.setData(data)

            // Indicate success and return the data
            withContext(Dispatchers.Main) {
                that.requestCoordinator.onApiRequestEnd()
            }
            return data

        } catch (e: Throwable) {

            // Indicate failure and throw the error
            val error = ErrorFactory().fromException(e)
            withContext(Dispatchers.Main) {
                that.requestCoordinator.onApiRequestEnd(error)
            }
            throw error
        }
    }

    /*
     * A standard algorithm for token refresh
     */
    @Suppress("ThrowsCount")
    private suspend fun <T> callApiWithTokenRefresh(
        method: String,
        url: String,
        dataToSend: Any?,
        causeError: Boolean,
        responseType: Class<T>,
    ): T? {

        // Avoid API requests when there is no access token, and instead trigger a login redirect
        var accessToken = this.oauthClient.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            throw ErrorFactory().fromLoginRequired()
        }

        try {

            // Call the API and return data on success
            return this.callApiWithToken(method, url, dataToSend, accessToken, causeError, responseType)

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
                return this.callApiWithToken(method, url, dataToSend, accessToken, causeError, responseType)

            } catch (e2: Throwable) {

                // Save retry errors
                val error2 = ErrorFactory().fromException(e2)
                if (error2.statusCode != 401) {
                    throw error2
                }

                // A permanent API 401 error triggers a new login.
                // This could be caused by an invalid API configuration.
                this.oauthClient.clearLoginState()
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
        dataToSend: Any?,
        accessToken: String,
        causeError: Boolean,
        responseType: Class<T>,
    ): T {

        // Configure the request body
        var body: RequestBody? = null
        if (dataToSend != null) {
            val gson = Gson()
            body = gson.toJson(dataToSend).toRequestBody("application/json".toMediaType())
        }

        // Build the full request
        val builder = Request
            .Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url(url)

        // Add a correlation ID for API logging
        builder.header("correlation-id", UUID.randomUUID().toString())

        // If required, add a header to request that an API simulates a 500 exception
        if (causeError) {
            builder.header("api-exception-simulation", "FinalApi")
        }

        val request = builder.build()

        // Create an HTTP client
        val client = OkHttpClient
            .Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        return suspendCoroutine { continuation ->

            client.newCall(request).enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {

                    if (response.isSuccessful) {

                        // Return deserialized data if the call succeeded
                        val responseData = Gson().fromJson(response.body.string(), responseType)
                        continuation.resumeWith(Result.success(responseData))

                    } else {

                        // Return response errors
                        val responseError =
                            ErrorFactory().fromHttpResponseError(response, url, "API")
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
