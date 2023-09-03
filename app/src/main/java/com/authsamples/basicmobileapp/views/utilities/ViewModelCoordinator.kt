package com.authsamples.basicmobileapp.views.utilities

import com.authsamples.basicmobileapp.api.client.FetchCache
import com.authsamples.basicmobileapp.api.client.FetchCacheKeys
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.events.LoginRequiredEvent
import com.authsamples.basicmobileapp.plumbing.events.ViewModelFetchEvent
import org.greenrobot.eventbus.EventBus

/*
 * Coordinates API requests from multiple views, and notifies once all API calls are complete
 * This ensures that login redirects are only triggered once
 */
class ViewModelCoordinator(
    private val fetchCache: FetchCache
) {

    private var isMainLoading = false
    private var isUserInfoLoading = false
    private var mainCacheKey = ""

    /*
     * This is called when the companies or transactions view model start sending API requests
     */
    fun onMainViewModelLoading() {

        // Send an event so that a subscriber can show a UI effect, such as disabling header buttons
        if (!this.isMainLoading) {
            this.isMainLoading = true
            EventBus.getDefault().post(ViewModelFetchEvent(false))
        }
    }

    /*
     * This is called when the companies or transactions view model receive an API response
     */
    fun onMainViewModelLoaded(cacheKey: String) {

        // Record the cache key so that we can look up its result later
        this.isMainLoading = false
        this.mainCacheKey = cacheKey

        // On success, send an event so that a subscriber can show a UI effect such as enabling header buttons
        val found = this.fetchCache.getItem(cacheKey)
        if (found?.getError() != null) {
            EventBus.getDefault().post(ViewModelFetchEvent(true))
        }

        // If all views have loaded, see if we need to trigger a login redirect
        this.triggerLoginIfRequired()
    }

    /*
     * Called when a non-main view model begins loading
     */
    fun onUserInfoViewModelLoading() {
        this.isUserInfoLoading = true
    }

    /*
     * This is called when the user info view model receives an API response
     * If all views have loaded, see if we need to trigger a login redirect
     */
    fun onUserInfoViewModelLoaded() {
        this.isUserInfoLoading = false
        this.triggerLoginIfRequired()
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
        this.isMainLoading = false
        this.isUserInfoLoading = false
        this.mainCacheKey = ""
    }

    /*
     * If all views are loaded and one or more has reported login required, then trigger a redirect
     */
    private fun triggerLoginIfRequired() {

        if (this.isAllViewsLoaded()) {

            val errors = this.getLoadErrors()
            val found = errors.find { e ->
                e.errorCode == ErrorCodes.loginRequired
            }

            if (found != null) {
                EventBus.getDefault().post(LoginRequiredEvent())
            }
        }
    }

    /*
     * See if all API requests have completed, and there are only 3 in the app
     */
    private fun isAllViewsLoaded(): Boolean {
        return !this.isMainLoading && !this.isUserInfoLoading
    }

    /*
     * Get the result of loading all views
     */
    private fun getLoadErrors(): List<UIError> {

        val errors: MutableList<UIError> = ArrayList()

        val keys: MutableList<String> = ArrayList()
        if (this.mainCacheKey.isNotBlank()) {
            keys.add(this.mainCacheKey)
        }
        keys.add(FetchCacheKeys.OAUTHUSERINFO)
        keys.add(FetchCacheKeys.APIUSERINFO)

        keys.forEach { key ->

            val found = this.fetchCache.getItem(key)
            val error = found?.getError()
            if (error != null) {
                errors.add(error)
            }
        }

        return errors
    }
}
