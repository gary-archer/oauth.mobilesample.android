package com.authguidance.basicmobileapp.plumbing.errors

import okhttp3.Response
import java.io.IOException
import com.google.gson.JsonParser
import net.openid.appauth.AuthorizationException

/*
 * A class to manage error translation
 */
class ErrorHandler {

    /*
     * Return an error from a general UI exception
     */
    fun fromException(ex: Exception): UIError {

        // Already handled
        if(ex is UIError) {
            return ex
        }

        // Create the error
        val error = UIError(
            "UI",
            "general_exception",
            "A technical problem was encountered in the UI")

        // Add exception specific details
        error.details = this.getErrorDescription(ex)
        error.addToStackTrace(ex)
        return error
    }

    /*
     * Return an error to short circuit execution
     */
    fun fromLoginRequired(): UIError {

        return UIError(
            "Login",
            "login_required",
            "A login is required so the API call was aborted")
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
     * Collect error details from the App Auth exception
     */
    fun fromAppAuthError(ex: AuthorizationException, errorCode: String): UIError {

        val error = UIError(
            "OAuth",
            errorCode,
            "An error was returned during a security operation"
        )

        if (ex.code != 0) {

            val appAuthErrorType = when {
                ex.type == 1 -> {
                    "AUTHORIZATION"
                }
                ex.type == 2 -> {
                    "TOKEN"
                }
                else -> {
                    "GENERAL"
                }
            }

            error.appAuthCode = "$appAuthErrorType / ${ex.code}"
        }

        if (ex.message != null) {
            error.details = ex.message
        }

        error.addToStackTrace(ex)
        return error
    }

    /*
     * Translate a failed API request into a presentable error response
     */
    fun fromApiRequestError(ex: IOException?, url: String): UIError {

        val error = UIError(
            "Network",
            "api_uncontactable",
            "A network problem occurred when the UI called the server")

        // Set other details
        if(ex != null) {
            error.details = this.getErrorDescription(ex)
        }

        error.url = url
        if(ex != null) {
            error.addToStackTrace(ex)
        }
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

        error.statusCode = response.code
        error.url = url

        this.updateFromApiErrorResponse(error, response.body?.string())
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

                // Try to read expected error fields
                val data = tree.asJsonObject
                val errorCode = data.get("code")
                val errorMessage = data.get("message")

                // Update the error object on success
                if(errorCode != null && errorMessage != null) {
                    error.errorCode = errorCode.asString
                    error.details = errorMessage.asString
                }
            }
        }
    }

    /*
     * Get the error message property's value
     */
    private fun getErrorDescription(ex: Exception): String? {

        if(ex.message != null) {
            return ex.message
        } else {
            return ex.javaClass.simpleName
        }
    }
}