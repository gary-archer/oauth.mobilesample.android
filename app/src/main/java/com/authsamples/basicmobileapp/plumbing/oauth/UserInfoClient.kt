package com.authsamples.basicmobileapp.plumbing.oauth

import com.authsamples.basicmobileapp.configuration.OAuthConfiguration
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * A helper to manage user info lookup
 */
class UserInfoClient(
    private val configuration: OAuthConfiguration,
    private val authenticator: Authenticator
) {

    /*
     * The entry point which manages retries
     */
    suspend fun getUserInfo(): OAuthUserInfo {

        // First get the current access token
        var accessToken = this.authenticator.getAccessToken()

        // Get metadata if required
        authenticator.getMetadata()

        // Make the userinfo request
        var response = this.makeUserInfoRequest(accessToken)
        if (response.isSuccessful) {

            // Handle successful responses
            return this.deserializeUserInfo(response)
        } else {

            // Retry once with a new token if there is a 401 error
            if (response.code == 401) {

                // Try to refresh the access token
                accessToken = authenticator.refreshAccessToken()

                // Retry the userinfo request
                response = this.makeUserInfoRequest(accessToken)
                if (response.isSuccessful) {
                    return this.deserializeUserInfo(response)

                } else {

                    // Handle failed responses on the retry
                    throw ErrorFactory().fromHttpResponseError(
                        response,
                        this.configuration.userInfoEndpoint,
                        "authorization server"
                    )
                }
            } else {

                // Handle failed responses on the original call
                throw ErrorFactory().fromHttpResponseError(
                    response,
                    this.configuration.userInfoEndpoint,
                    "authorization server"
                )
            }
        }
    }

    /*
     * Make a user info request with the current access token
     */
    private suspend fun makeUserInfoRequest(accessToken: String): Response {

        val body: RequestBody? = null
        val builder = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method("GET", body)
            .url(this.configuration.userInfoEndpoint)
        val request = builder.build()

        val client = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        val that = this@UserInfoClient
        return suspendCoroutine { continuation ->

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {

                    continuation.resumeWith(Result.success(response))
                }

                override fun onFailure(call: Call, e: IOException) {

                    val exception = ErrorFactory().fromHttpRequestError(
                        e,
                        that.configuration.userInfoEndpoint,
                        "authorization server"
                    )
                    continuation.resumeWithException(exception)
                }
            })
        }
    }

    /*
     * Deserialize the response data for user info
     */
    private fun deserializeUserInfo(response: Response): OAuthUserInfo {

        if (response.body == null) {
            throw IllegalStateException("Unable to deserialize HTTP response into user info")
        }

        val tree = JsonParser.parseString(response.body?.string())
        val data = tree.asJsonObject
        val givenName = data.get("given_name")?.asString ?: ""
        val familyName = data.get("family_name")?.asString ?: ""
        return OAuthUserInfo(givenName, familyName)
    }
}