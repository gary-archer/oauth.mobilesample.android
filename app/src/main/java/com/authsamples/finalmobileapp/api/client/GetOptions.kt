package com.authsamples.finalmobileapp.api.client

/*
 * Input when making a cacheable GET request
 */
data class GetOptions(
    val forceReload: Boolean,
    val causeError: Boolean,
)
