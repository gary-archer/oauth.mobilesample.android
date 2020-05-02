package com.authguidance.basicmobileapp.views

import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError

/*
 * A helper class to coordinate multiple views
 */
class ViewManager(
    private val onLoadStateChanged: (loaded: Boolean) -> Unit,
    private val onLoginRequired: () -> Unit
) {
    // Properties
    private var viewsToLoad: Int = 1
    private var loadedCount: Int = 0
    private var hasErrors: Boolean = false
    private var loginRequired: Boolean = false

    /*
     * Allow the parent to set the number of views to load
     */
    fun setViewCount(count: Int) {
        this.viewsToLoad = count
    }

    /*
     * Handle the view loading event and inform the parent, which can render a loading state
     */
    fun onViewLoading() {
        this.onLoadStateChanged(false)
    }

    /*
     * Handle the view loaded event and call back the parent when all loading is complete
     */
    fun onViewLoaded() {

        this.loadedCount += 1

        // Once all views have loaded, inform the parent if all views loaded successfully
        if (this.loadedCount == this.viewsToLoad) {

            this.reset()
            if (!this.hasErrors) {
                this.onLoadStateChanged(true)
            }
        }
    }

    /*
     * Handle the view load failed event
     */
    fun onViewLoadFailed(error: UIError) {

        this.loadedCount += 1
        this.hasErrors = true

        // Record if this was a login required error
        if (error.errorCode == ErrorCodes.loginRequired) {
            this.loginRequired = true
        }

        // Once all views have loaded, reset state and, if required, trigger a login redirect only once
        if (this.loadedCount == this.viewsToLoad) {

            val triggerLoginOnParent = this.loginRequired
            this.reset()

            if (triggerLoginOnParent) {
                this.onLoginRequired()
            }
        }
    }

    /*
     * Reset to the initial state once loading is complete
     * Default to loading a single view, unless the parent informs us otherwise
     */
    private fun reset() {
        this.viewsToLoad = 1
        this.loadedCount = 0
        this.hasErrors = false
        this.loginRequired = false
    }
}
