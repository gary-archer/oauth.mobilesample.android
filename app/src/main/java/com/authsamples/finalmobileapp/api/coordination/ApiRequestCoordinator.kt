package com.authsamples.finalmobileapp.api.coordination

import com.authsamples.finalmobileapp.api.client.FetchCache
import com.authsamples.finalmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.plumbing.events.ApiRequestEvent
import com.authsamples.finalmobileapp.plumbing.events.LoginRequiredEvent
import org.greenrobot.eventbus.EventBus

/*
 * Coordinates API requests from multiple views, and handles session expiry when required
 */
class ApiRequestCoordinator(
    private val eventBus: EventBus,
    private val fetchCache: FetchCache,
) {

    private val cacheKeys: MutableList<String> = ArrayList()
    private var loadingCount = 0
    private var loadedCount = 0

    /*
     * This is called when a view model starts sending API requests
     */
    fun onApiRequestStart(vararg keys: String) {

        if (this.loadingCount == 0) {
            this.eventBus.post(ApiRequestEvent(false))
        }
        ++this.loadingCount
        this.cacheKeys.addAll(keys.toList())
    }

    /*
     * This is called when a view model receives an API response
     */
    fun onApiRequestEnd() {

        ++this.loadedCount

        // On success, send an event so that a subscriber can show a UI effect such as enabling header buttons
        val hasData = this.cacheKeys.any { key ->
            this.fetchCache.getItem(key)?.getData() != null
        }
        if (hasData) {
            this.eventBus.post(ApiRequestEvent(true))
        }

        // Perform error logic after all views have loaded
        this.handleAllViewsLoaded()
    }

    /*
     * Return true if there were any load errors
     */
    fun hasErrors(): Boolean {
        return this.getLoadErrors().isNotEmpty()
    }

    /*
     * Reset state when the Reload Data button is clicked
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
    private fun handleAllViewsLoaded() {

        if (this.loadedCount == this.loadingCount) {

            // Reset counts
            this.loadingCount = 0
            this.loadedCount = 0

            // Login required errors occur when there are no tokens yet or when token refresh fails
            // The sample's user behavior is to automatically redirect the user to login
            val errors = this.getLoadErrors()
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
