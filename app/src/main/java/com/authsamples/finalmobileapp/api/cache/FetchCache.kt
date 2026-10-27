package com.authsamples.finalmobileapp.api.cache

import java.util.concurrent.ConcurrentHashMap

/*
 * A cache to prevent redundant HTTP requests
 * This is used when the data for a view has already been retrieved
 * This includes during back navigation and view recreation by the Android system
 */
class FetchCache {

    // A map of cache keys to API responses
    private val cache: ConcurrentHashMap<String, Any> = ConcurrentHashMap<String, Any>()

    /*
     * Create an item with no data when an API request is triggered
     */
    fun setItem(key: String, data: Any) {
        this.cache[key] = data
    }

    /*
     * Get an item if it exists
     */
    fun getItem(key: String): Any? {
        return this.cache[key]
    }

    /*
     * Remove an item when forcing a reload
     */
    fun removeItem(key: String) {
        this.cache.remove(key)
    }

    /*
     * Clear the cache when logging out
     */
    fun clearAll() {
        this.cache.clear()
    }
}
