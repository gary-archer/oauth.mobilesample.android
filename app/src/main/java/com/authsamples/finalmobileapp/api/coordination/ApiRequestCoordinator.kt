package com.authsamples.finalmobileapp.api.coordination

import com.authsamples.finalmobileapp.api.client.FetchCache
import com.authsamples.finalmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.plumbing.events.ApiRequestEvent
import com.authsamples.finalmobileapp.plumbing.events.LoginRequiredEvent
import org.greenrobot.eventbus.EventBus

/*
 * Called on the main thread to coordinate API requests from multiple views, and handles session expiry when required
 */
class ApiRequestCoordinator(
    private val eventBus: EventBus,
    private val fetchCache: FetchCache,
) {

    private var cacheKeys: MutableList<String> = ArrayList()
    private var loadingCount = 0
    private var loadedCount = 0

    /*
     * Called by a view model when it starts an API request
     * Accepts one or more cache keys to support view models that make concurrent requests
     * Fires ApiRequestEvent(false) on the first request of a batch to signal loading has begun
     */
    fun onApiRequestStart(vararg keys: String) {

        if (this.loadingCount == 0) {
            this.cacheKeys = ArrayList()
        }

        this.cacheKeys.addAll(keys.toList())
        ++this.loadingCount

        if (this.loadingCount == 1) {
            this.eventBus.post(ApiRequestEvent(false))
        }
    }

    /*
     * Called by a view model when its API request completes
     * Fires ApiRequestEvent(true) once all pending requests in the batch have finished
     */
    fun onApiRequestEnd() {

        ++this.loadedCount
        this.handleAllRequestsComplete()
    }

    /*
     * Return true if there were any load errors
     */
    fun hasErrors(): Boolean {
        return this.getLoadErrors().isNotEmpty()
    }

    /*
     * Reset state when the Reload Data button is clicked or on logout
     */
    fun resetState() {
        this.loadingCount = 0
        this.loadedCount = 0
        this.cacheKeys.clear()
        this.fetchCache.clearAll()
    }

    /*
     * Handle OAuth related errors once all views finish loading
     */
    private fun handleAllRequestsComplete() {

        if (this.loadedCount == this.loadingCount) {

            // Reset counts
            this.loadingCount = 0
            this.loadedCount = 0

            // Notify subscribers when all API requests have completed successfully
            val errors = this.getLoadErrors()
            if (errors.count() == 0) {
                this.eventBus.post(ApiRequestEvent(true))
            }

            // Login required errors occur when there are no tokens yet or when token refresh fails
            // Raise a single event so that navigation to login required is triggered only once
            val loginRequired = errors.find { e -> e.errorCode == ErrorCodes.LOGIN_REQUIRED }
            if (loginRequired != null) {
                this.resetState()
                this.eventBus.post(LoginRequiredEvent())
                return
            }
        }
    }

    /*
     * Get the result of loading all views
     */
    private fun getLoadErrors(): List<UIError> {

        val errors: MutableList<UIError> = ArrayList()

        this.cacheKeys.forEach { key ->
            val found = this.fetchCache.getItem(key)
            val error = found?.getError()
            if (error != null) {
                errors.add(error)
            }
        }

        return errors
    }
}
