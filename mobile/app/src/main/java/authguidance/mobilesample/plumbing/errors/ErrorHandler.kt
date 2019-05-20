package authguidance.mobilesample.plumbing.errors

import okhttp3.Response
import java.io.IOException

/*
 * A class to manage error translation
 */
class ErrorHandler {

    /*
     * Return an error from a general UI exception
     */
    fun fromException(exception: Exception): UIError {

        // Already handled
        if(exception is UIError) {
            return exception;
        }

        // Create the error
        val error = UIError(
            "UI",
            "general_exception",
            "A technical problem was encountered in the UI")

        // TODO: Set other fields
        return error
    }

    /*
     * Translate a failed API request into a presentable error response
     */
    fun fromApiRequestError(exception: IOException?, url: String): UIError {

        val error = UIError(
            "Network",
            "api_uncontactable",
            "A network problem occurred when the UI called the server")

        // Previous code
        // RuntimeException("Error making HTTP request", e)

        // TODO: Set other fields
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

        // Previous code
        // var result = "Status: ${it.code()} : ${it.body()?.string()}";

        // TODO: Set other fields
        this.updateFromApiErrorResponse(error, response)
        return error
    }

    /*
     * Try to update the default API error with response details
     */
    private fun updateFromApiErrorResponse(error: UIError, response: Response) {
    }
}