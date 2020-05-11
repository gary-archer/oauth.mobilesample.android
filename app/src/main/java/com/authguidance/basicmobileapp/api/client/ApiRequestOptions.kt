package com.authguidance.basicmobileapp.api.client

/*
 * Special options when making an API request
 */
data class ApiRequestOptions(

    // We can send an option to make the API fail, to demonstrate 500 handling
    val causeError: Boolean
)
