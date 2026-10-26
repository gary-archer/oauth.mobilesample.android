package com.authsamples.finalmobileapp.api.client

/*
 * Input when making a cacheable GET request
 */
data class GetOptions(
    val cacheKey: String,
    val forceReload: Boolean,
    val causeError: Boolean,
)
