package com.authguidance.basicmobileapp.views.utilities

/*
 * Constant values
 */
object Constants {

    // The request code for OAuth related activities
    const val LOGIN_REDIRECT_REQUEST_CODE = 1
    const val LOGOUT_REDIRECT_REQUEST_CODE = 2

    // The request code invoking the lock screen settings activity
    const val SET_LOCK_SCREEN_REQUEST_CODE = 3

    // The company id passed when navigating to the transactions fragment
    const val ARG_COMPANY_ID = "COMPANY_ID"

    // Error details passed when navigating to error fragments
    const val ARG_ERROR_DATA = "ERROR_DATA"
    const val ARG_ERROR_TITLE = "ERROR_TITLE"

    // Names of view areas that calls APIs, used by ApiViewEvents
    const val VIEW_MAIN = "VIEW_MAIN"
    const val VIEW_USERINFO = "VIEW_USERINFO"
}
