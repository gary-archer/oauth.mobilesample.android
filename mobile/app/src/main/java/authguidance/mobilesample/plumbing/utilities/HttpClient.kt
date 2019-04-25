package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.plumbing.oauth.Authenticator
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient

/*
 * Logic related to making HTTP calls, and we follow the type of modern approach from this article
 * https://android.jlelse.eu/android-networking-in-2019-retrofit-with-kotlins-coroutines-aefe82c4d777
 */
class HttpClient(authenticator: Authenticator) {

    private val _authenticator = authenticator;

    /*
     * The entry point for calling an API in a parameterised manner
     */
    suspend fun callApi(method: String, url: String): Deferred<String> {

        val accessToken = _authenticator.getAccessToken()

        var logger = MobileLogger()
        logger.debug("Calling API over $url")

        // Get a request object
        val client = OkHttpClient().newBuilder().build()

        // Make the request and return an object
    }

        /*
        // Syntax from here
        // https://github.com/kittinunf/fuel/blob/master/fuel-coroutines/README.md
        return GlobalScope.async {

            val (_, response, result) = Fuel.get(url).awaitStringResponseResult()

            var status = 0
            result.fold(
                { data -> status = 200 },
                { error -> status = error.response.statusCode }
            )

            logger.debug("RESPONSE RECEIVED")
            logger.debug(status.toString())

            // Return the result
            // response.statusCode
            status
        }*/
}
