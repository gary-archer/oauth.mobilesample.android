package com.authsamples.finalmobileapp.api.cache

/*
 * A cache item represents a successful API response
 */
class FetchCacheItem {

    private var data: Any?

    init {
        this.data = null
    }

    fun getData(): Any? {
        return this.data
    }

    fun setData(value: Any?) {
        this.data = value
    }
}
