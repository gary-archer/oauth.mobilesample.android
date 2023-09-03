package com.authsamples.basicmobileapp.api.client

/*
 * Input when making a cacheable fetch request
 */
data class FetchOptions(
    val cacheKey: String,
    val forceReload: Boolean,
    val causeError: Boolean
)
