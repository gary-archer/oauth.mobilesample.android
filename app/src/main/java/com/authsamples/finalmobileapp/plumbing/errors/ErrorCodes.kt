package com.authsamples.finalmobileapp.plumbing.errors

/*
 * Error codes that the UI can program against
 */
object ErrorCodes {

    // Used to indicate that the API cannot be called until the user logs in
    const val loginRequired = "login_required"

    // Used to indicate that metadata lookup failed
    const val metadataLookup = "metadata_lookup"

    // Used to indicate that the Chrome Custom Tab was cancelled
    const val redirectCancelled = "redirect_cancelled"

    // A technical error starting a login request, such as contacting the metadata endpoint
    const val loginRequestFailed = "login_request_failed"

    // A technical error processing the login response containing the authorization code
    const val loginResponseFailed = "login_response_failed"

    // A technical error exchanging the authorization code for tokens
    const val authorizationCodeGrantFailed = "authorization_code_grant"

    // A technical problem during background token renewal
    const val tokenRenewalError = "token_renewal_error"

    // Indicate when logout is not supported
    const val logoutNotSupported = "logout_not_supported"

    // An error starting a logout request, such as contacting the metadata endpoint
    const val logoutRequestFailed = "logout_request_failed"

    // Returned from APIs when an access token is rejected
    const val invalidToken = "invalid_token"

    // Returned from APIs when an access token does not have the required scope
    const val insufficientScope = "insufficient_scope"

    // Returned from APIs when it cannot find the claims it needs in access tokens
    const val claimsFailure = "claims_failure"

    // A general exception in the UI
    const val generalUIError = "ui_error"

    // An error making an API call to get data
    const val apiNetworkError = "api_network_error"

    // An error response fropm the API
    const val apiResponseError = "api_response_error"

    // Returned by the API when the user edits the browser URL and ties to access an unauthorised company
    const val companyNotFound = "company_not_found"

    // Returned by the API when the user edits the browser URL and supplies a non numeric company id
    const val invalidCompanyId = "invalid_company_id"
}
