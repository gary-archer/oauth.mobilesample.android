package com.authsamples.finalmobileapp.api.cache

/*
 * Input when making a cacheable fetch request
 */
data class FetchOptions(
    val cacheKey: String,
    val forceReload: Boolean,
    val causeError: Boolean,
)
