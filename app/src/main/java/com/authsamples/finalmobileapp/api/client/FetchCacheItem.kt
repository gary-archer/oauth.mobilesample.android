package com.authsamples.finalmobileapp.api.client

import com.authsamples.finalmobileapp.plumbing.errors.UIError

/*
 * A cache item represents an API response
 */
class FetchCacheItem {

    var isLoading: Boolean
    private var data: Any?
    private var error: UIError?

    init {
        this.isLoading = true
        this.data = null
        this.error = null
    }

    fun getData(): Any? {
        return this.data
    }

    fun setData(value: Any?) {
        this.data = value
        this.isLoading = false
    }

    fun getError(): UIError? {
        return this.error
    }

    fun setError(value: UIError?) {
        this.error = value
        this.isLoading = false
    }
}
