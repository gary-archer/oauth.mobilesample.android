package authguidance.mobilesample.plumbing.errors

import okhttp3.Response
import java.io.IOException
import com.google.gson.JsonParser

/*
 * A class to manage error translation
 */
class ErrorHandler {

    /*
     * Return an error from a general UI exception
     */
    fun fromException(exception: Throwable): UIError {

        // Already handled
        if(exception is UIError) {
            return exception
        }

        // Create the error
        val error = UIError(
            "UI",
            "general_exception",
            "A technical problem was encountered in the UI")
        error.details = this.getErrorDescription(exception)
        return error
    }

    /*
     * Return an error to indicate that the Chrome custom tab window was closed
     */
    fun fromLoginCancelled(): UIError {

        return UIError(
            "Login",
            "login_cancelled",
            "The login request was cancelled")
    }

    /*
     * Translate a failed API request into a presentable error response
     */
    fun fromApiRequestError(exception: IOException?, url: String): UIError {

        val error = UIError(
            "Network",
            "api_uncontactable",
            "A network problem occurred when the UI called the server")

        // Set other details
        if(exception != null) {
            error.details = this.getErrorDescription(exception)
        }

        error.url = url
        return error
    }

    /*
     * Translate a failed API response into a presentable error response
     */
    fun fromApiResponseError(response: Response, url: String): UIError {

        val error = UIError(
            "API",
            "general_api_error",
            "A technical problem occurred when the UI called the server")

        error.statusCode = response.code()
        error.url = url

        this.updateFromApiErrorResponse(error, response.body()?.string())
        return error
    }

    /*
     * Try to update the default API error with response details
     */
    private fun updateFromApiErrorResponse(error: UIError, response: String?) {

        if(response != null) {
            val parser = JsonParser()
            val tree = parser.parse(response)
            if(tree != null) {

                // Read raw properties for now
                val data = tree.asJsonObject
                error.details = data.get("message").asString
            }
        }
    }

    /*
     * Get the error message property's value
     */
    private fun getErrorDescription(error: Throwable): String {

        val result = error.message
        return result ?: error.toString()
    }
}