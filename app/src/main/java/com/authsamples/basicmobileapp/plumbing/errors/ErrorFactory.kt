package com.authsamples.basicmobileapp.plumbing.errors

import com.google.gson.JsonParser
import net.openid.appauth.AuthorizationException
import okhttp3.Response
import java.io.IOException
import java.util.Locale

/*
 * A class to manage processing errors and translation to a presentation format
 */
class ErrorFactory {

    /*
     * Return an error from a general UI exception
     */
    fun fromException(ex: Throwable): UIError {

        // Already handled
        if (ex is UIError) {
            return ex
        }

        val error = UIError(
            "Mobile UI",
            ErrorCodes.generalUIError,
            "A technical problem was encountered in the UI"
        )

        this.updateFromException(ex, error)
        return error
    }

    /*
     * Return an error to short circuit execution
     */
    fun fromLoginRequired(): UIError {

        return UIError(
            "Login",
            ErrorCodes.loginRequired,
            "A login is required so the API call was aborted"
        )
    }

    /*
     * Indicate a metadata lookup failure
     */
    fun fromMetadataLookupError(ex: Throwable): UIError {

        val error = UIError(
            "Login",
            ErrorCodes.metadataLookup,
            "Problem encountered downloading OpenID Connect metadata"
        )

        this.updateFromException(ex, error)
        return error
    }

    /*
     * Return an error to indicate that the Chrome custom tab window was closed
     */
    fun fromRedirectCancelled(): UIError {

        return UIError(
            "Login",
            ErrorCodes.redirectCancelled,
            "The login request was cancelled"
        )
    }

    /*
     * Handle errors signing in
     */
    fun fromLoginOperationError(ex: Throwable, errorCode: String): UIError {

        // Already handled
        if (ex is UIError) {
            return ex
        }

        val error = UIError(
            "Login",
            errorCode,
            "A technical problem occurred during login processing"
        )

        if (ex is AuthorizationException) {
            this.updateFromAppAuthException(ex, error)
        } else {
            this.updateFromException(ex, error)
        }

        return error
    }

    /*
     * Return an error to indicate that there is no end session endpoint
     */
    fun fromLogoutNotSupportedError(): UIError {

        return UIError(
            "Logout",
            ErrorCodes.logoutNotSupported,
            "Logout cannot be invoked because there is no end session endpoint"
        )
    }

    /*
     * Handle errors signing out
     */
    fun fromLogoutOperationError(ex: Throwable): UIError {

        // Already handled
        if (ex is UIError) {
            return ex
        }

        val error = UIError(
            "Logout",
            ErrorCodes.logoutRequestFailed,
            "A technical problem occurred during logout processing"
        )

        this.updateFromException(ex, error)
        return error
    }

    /*
     * Handle errors from the token endpoint
     */
    fun fromTokenError(ex: Throwable, errorCode: String): UIError {

        // Already handled
        if (ex is UIError) {
            return ex
        }

        val error = UIError(
            "Token",
            errorCode,
            "A technical problem occurred during token processing"
        )

        if (ex is AuthorizationException) {
            this.updateFromAppAuthException(ex, error)
        } else {
            this.updateFromException(ex, error)
        }

        return error
    }

    /*
     * Translate a failed API request into a presentable error response
     */
    fun fromHttpRequestError(ex: IOException?, url: String, source: String): UIError {

        val error = UIError(
            "Network",
            ErrorCodes.apiNetworkError,
            "A network problem occurred when the UI called the $source"
        )

        // Set other details
        if (ex != null) {
            error.details = this.getErrorDescription(ex)
        }

        error.url = url
        if (ex != null) {
            error.stackTrace = ex.stackTrace
        }
        return error
    }

    /*
     * Translate a failed HTTP response into a presentable error response
     */
    fun fromHttpResponseError(response: Response, url: String, source: String): UIError {

        val error = UIError(
            "API",
            ErrorCodes.apiResponseError,
            "A technical problem occurred when the UI called the $source"
        )

        error.statusCode = response.code
        error.url = url

        val contentType = response.headers["content-type"]?.lowercase(Locale.ROOT)
        if (contentType == "application/json") {
            if (response.body != null) {
                this.updateFromErrorResponseBody(error, response.body!!.string())
            }
        }

        return error
    }

    /*
     * Get details from the underlying exception
     */
    private fun updateFromException(ex: Throwable, error: UIError) {

        error.details = this.getErrorDescription(ex)
        error.stackTrace = ex.stackTrace
    }

    /*
     * Add AppAuth details to our standard error object
     */
    private fun updateFromAppAuthException(ex: AuthorizationException, error: UIError) {

        if (ex.code != 0) {

            val appAuthErrorType = when (ex.type) {
                1 -> {
                    "AUTHORIZATION"
                }
                2 -> {
                    "TOKEN"
                }
                else -> {
                    "GENERAL"
                }
            }

            error.appAuthCode = "$appAuthErrorType / ${ex.code}"
        }

        this.updateFromException(ex, error)
    }

    /*
     * Try to update the default HTTP error with response details
     */
    private fun updateFromErrorResponseBody(error: UIError, response: String?) {

        if (response == null) {
            return
        }

        val tree = JsonParser.parseString(response) ?: return

        // Try to read expected error fields
        val data = tree.asJsonObject
        var errorCode = data.get("code")
        var errorMessage = data.get("message")

        // Handle API errors, which include extra details for 5xx errors
        if (errorCode != null && errorMessage != null) {

            error.errorCode = errorCode.asString
            error.details = errorMessage.asString

            val area = data.get("area")
            val id = data.get("id")
            val utcTime = data.get("utcTime")
            if (area != null && id != null && utcTime != null) {
                error.setApiErrorDetails(area.asString, id.asInt, utcTime.asString)
            }
        }

        // Handle OAuth errors in HTTP reponses
        errorCode = data.get("error")
        errorMessage = data.get("error_description")
        if (errorCode != null && errorMessage != null) {
            error.errorCode = errorCode.asString
            error.details = errorMessage.asString
        }
    }

    /*
     * Get the error message property's value
     */
    private fun getErrorDescription(ex: Throwable): String? {

        return if (ex.message != null) {
            ex.message
        } else {
            ex.javaClass.simpleName
        }
    }
}
