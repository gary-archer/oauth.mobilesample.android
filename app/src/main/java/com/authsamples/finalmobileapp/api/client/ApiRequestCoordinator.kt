package com.authsamples.finalmobileapp.api.client

import com.authsamples.finalmobileapp.api.cache.FetchCache
import com.authsamples.finalmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.plumbing.events.ApiRequestEvent
import com.authsamples.finalmobileapp.plumbing.events.LoginRequiredEvent
import org.greenrobot.eventbus.EventBus

/*
 * Called on the main thread to coordinate API requests from multiple views, and handles session expiry when required
 */
class ApiRequestCoordinator(
    private val fetchCache: FetchCache,
    private val eventBus: EventBus,
) {

    private var cacheKeys: MutableList<String> = ArrayList()
    private var loadingCount = 0
    private var loadedCount = 0
    private var errors: List<UIError> = ArrayList()

    /*
     * Called when an API request starts
     * Fires ApiRequestEvent(false) on the first request of a batch to signal loading has begun
     */
    fun onApiRequestStart(key: String) {

        if (this.loadingCount == 0) {
            this.cacheKeys = ArrayList()
            this.eventBus.post(ApiRequestEvent(false))
        }

        this.cacheKeys.add(key)
        ++this.loadingCount
    }

    /*
     * Called when an API request ends
     * Fires ApiRequestEvent(true) once all pending requests in the batch have finished
     */
    fun onApiRequestEnd() {

        ++this.loadedCount
        this.handleAllRequestsComplete()
    }

    /*
     * Return true if there were any errors on the last batch of API requests
     */
    fun hasErrors(): Boolean {
        return this.errors.count() > 0
    }

    /*
     * Reset state when the Reload Data button is clicked or on logout
     */
    fun resetState() {
        this.errors = ArrayList()
        this.cacheKeys.clear()
        this.loadingCount = 0
        this.loadedCount = 0
    }

    /*
     * Handle OAuth related errors once all views finish loading
     */
    private fun handleAllRequestsComplete() {

        if (this.loadedCount == this.loadingCount) {

            // Reset counts
            this.loadingCount = 0
            this.loadedCount = 0

            // Get error details stored in the fetch cache
            this.errors = this.getAllErrors()

            // Notify subscribers when all API requests have completed successfully
            if (this.errors.count() == 0) {
                this.eventBus.post(ApiRequestEvent(true))
            }

            // Login required errors occur when there are no tokens yet or when token refresh fails
            // Raise a single event so that navigation to login required is triggered only once
            val loginRequired = this.errors.find { e -> e.errorCode == ErrorCodes.LOGIN_REQUIRED }
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
    private fun getAllErrors(): List<UIError> {

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
