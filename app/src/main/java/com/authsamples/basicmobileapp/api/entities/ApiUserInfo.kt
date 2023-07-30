package com.authsamples.basicmobileapp.api.entities

/*
 * User attributes from the API's own data
 */
data class ApiUserInfo(
    val role: String,
    val regions: ArrayList<String>
)
