package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.plumbing.oauth.Authenticator
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/*
 * Logic related to making HTTP calls
 */
class HttpClient(authenticator: Authenticator) {

    private val _authenticator = authenticator;

    /*
     * The entry point for calling an API
     * For now this just returns the status code
     */
    suspend fun callApi(url: String): Deferred<Int> {

        _authenticator.getAccessToken()

        var logger = MobileLogger();
        logger.debug("Calling API over $url");

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
        }
    }
}
