package com.authsamples.finalmobileapp.api.client

/*
 * Input when making a cacheable fetch request
 */
data class FetchOptions(
    val cacheKey: String,
    val forceReload: Boolean,
    val causeError: Boolean
)
