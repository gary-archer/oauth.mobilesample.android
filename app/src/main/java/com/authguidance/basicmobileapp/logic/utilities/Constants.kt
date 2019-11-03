package com.authguidance.basicmobileapp.logic.utilities

/*
 * Constant values
 */
object Constants {

    // The request code for setting the lock screen
    const val SET_LOCK_SCREEN_REQUEST_CODE = 1

    // The request code for OAuth related redirects
    const val LOGIN_REDIRECT_REQUEST_CODE = 2
    const val LOGOUT_REDIRECT_REQUEST_CODE = 3

    // The company id passed when navigating to the transactions fragment
    const val ARG_COMPANY_ID = "COMPANY_ID"

    // Error details passed when navigating to the error fragment
    const val ARG_ERROR_DATA = "ERROR_DATA"
    const val ARG_ERROR_DEBUG = "ERROR_DEBUG"
}