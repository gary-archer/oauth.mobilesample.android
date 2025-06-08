package com.authsamples.finalmobileapp.api.client

import java.util.concurrent.ConcurrentHashMap

/*
 * A cache to prevent redundant HTTP requests
 * This is used when the data for a view has already been retrieved
 * This includes during back navigation and view recreation by the Android system
 */
class FetchCache {

    // A map of cache keys to API responses
    private val cache: ConcurrentHashMap<String, FetchCacheItem> = ConcurrentHashMap<String, FetchCacheItem>()

    /*
     * Create an item with no data when an API request is triggered
     */
    fun createItem(key: String): FetchCacheItem {

        var item = this.getItem(key)
        if (item == null) {

            item = FetchCacheItem()
            this.cache[key] = item
        }

        return item
    }

    /*
     * Get an item if it exists
     */
    fun getItem(key: String): FetchCacheItem? {
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
