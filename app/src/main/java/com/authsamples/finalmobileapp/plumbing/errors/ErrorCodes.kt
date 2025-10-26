package com.authsamples.finalmobileapp.plumbing.errors

/*
 * Error codes that the UI can program against
 */
object ErrorCodes {

    // Used to indicate that the API cannot be called until the user logs in
    const val LOGIN_REQUIRED = "login_required"

    // Used to indicate that metadata lookup failed
    const val METADATA_LOOKUP = "metadata_lookup"

    // Used to indicate that the Chrome Custom Tab was cancelled
    const val REDIRECT_CANCELLED = "redirect_cancelled"

    // A technical error starting a login request, such as contacting the metadata endpoint
    const val LOGIN_REQUEST_FAILED = "login_request_failed"

    // A technical error processing the login response containing the authorization code
    const val LOGIN_RESPONSE_FAILED = "login_response_failed"

    // A technical error exchanging the authorization code for tokens
    const val AUTHORIZATION_CODE_GRANT_FAILED = "authorization_code_grant"

    // A technical problem during background token renewal
    const val TOKEN_RENEWAL_ERROR = "token_renewal_error"

    // Indicate when logout is not supported
    const val LOGOUT_NOT_SUPPORTED = "logout_not_supported"

    // An error starting a logout request, such as contacting the metadata endpoint
    const val LOGOUT_REQUEST_FAILED = "logout_request_failed"

    // A general exception in the UI
    const val GENERAL_UI_ERROR = "ui_error"

    // An error making an API call to get data
    const val API_NETWORK_ERROR = "api_network_error"

    // An error response fropm the API
    const val API_RESPONSE_ERROR = "api_response_error"

    // Returned by the API when the user edits the browser URL and ties to access an unauthorised company
    const val COMPANY_NOT_FOUND = "company_not_found"

    // Returned by the API when the user edits the browser URL and supplies a non numeric company id
    const val INVALID_COMPANY_ID = "invalid_company_id"
}
