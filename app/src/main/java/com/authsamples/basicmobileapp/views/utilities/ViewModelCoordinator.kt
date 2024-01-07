package com.authsamples.basicmobileapp.views.utilities

import com.authsamples.basicmobileapp.api.client.FetchCache
import com.authsamples.basicmobileapp.api.client.FetchCacheKeys
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.events.LoginRequiredEvent
import com.authsamples.basicmobileapp.plumbing.events.ViewModelFetchEvent
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import org.greenrobot.eventbus.EventBus

/*
 * Coordinates API requests from multiple views, and notifies once all API calls are complete
 * This ensures that login redirects are only triggered once
 */
class ViewModelCoordinator(
    private val eventBus: EventBus,
    private val fetchCache: FetchCache,
    private val authenticator: Authenticator
) {

    private var mainCacheKey = ""
    private var loadingCount = 0
    private var loadedCount = 0

    /*
     * This is called when the companies or transactions view model start sending API requests
     */
    fun onMainViewModelLoading() {

        // Update stats
        ++this.loadingCount

        // Send an event so that a subscriber can show a UI effect, such as disabling header buttons
        this.eventBus.post(ViewModelFetchEvent(false))
    }

    /*
     * This is called when the companies or transactions view model receive an API response
     */
    fun onMainViewModelLoaded(cacheKey: String) {

        // Record the cache key so that we can look up its result later
        this.mainCacheKey = cacheKey
        ++this.loadedCount

        // On success, send an event so that a subscriber can show a UI effect such as enabling header buttons
        val found = this.fetchCache.getItem(cacheKey)
        if (found?.getData() != null) {
            this.eventBus.post(ViewModelFetchEvent(true))
        }

        // Perform error logic after all views have loaded
        this.handleErrorsAfterLoad()
    }

    /*
     * Called when a non-main view model begins loading
     */
    fun onUserInfoViewModelLoading() {
        ++this.loadingCount
    }

    /*
     * This is called when the user info view model receives an API response
     * If all views have loaded, see if we need to trigger a login redirect
     */
    fun onUserInfoViewModelLoaded() {
        ++this.loadedCount
        this.handleErrorsAfterLoad()
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
        this.mainCacheKey = ""
    }

    /*
     * Handle OAuth related errors
     */
    private fun handleErrorsAfterLoad() {

        if (this.loadedCount == this.loadingCount) {

            val errors = this.getLoadErrors()

            // Login required errors occur when there are no tokens yet or when token refresh fails
            // The sample's user behavior is to automatically redirect the user to login
            val loginRequired = errors.find { e -> e.errorCode == ErrorCodes.loginRequired }
            if (loginRequired != null) {
                this.eventBus.post(LoginRequiredEvent())
                return
            }

            // In normal conditions the following errors are likely to be OAuth configuration errors
            @Suppress("Indentation")
            val oauthConfigurationError = errors.find { e ->
                (e.statusCode == 401 && e.errorCode == ErrorCodes.invalidToken) ||
                (e.statusCode == 403 && e.errorCode == ErrorCodes.insufficientScope)
            }

            // The sample's user behavior is to present an error, after which clicking Home runs a new login redirect
            // This allows the frontend application to get new tokens, which may fix the problem in some cases
            if (oauthConfigurationError != null) {
                this.authenticator.clearLoginState()
            }
        }
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
