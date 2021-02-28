package com.authguidance.basicmobileapp.views.utilities

import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.utilities.Constants.VIEW_MAIN

/*
 * A helper class to coordinate multiple views
 */
class ApiViewEvents(
    val onLoginRequiredAction: () -> Unit,
    val onMainLoadStateChanged: (loaded: Boolean) -> Unit
) {
    // A map of view names to their loaded state
    private val views: MutableMap<String, Boolean>

    // An overall login required flag
    private var loginRequired: Boolean

    /*
     * Set initial state
     */
    init {
        this.views = mutableMapOf()
        this.loginRequired = false
    }

    /*
     * Each view is added along with an initial unloaded state
     */
    fun addView(name: String) {
        this.views.put(name, false)
    }

    /*
     * Handle loading notifications, which will disable the header buttons
     */
    fun onViewLoading(name: String) {

        views[name] = false

        if (name == VIEW_MAIN) {
            this.onMainLoadStateChanged(false)
        }
    }

    /*
     * Update state when loaded, which may trigger a login redirect once all views are loaded
     */
    fun onViewLoaded(name: String) {

        views[name] = true

        if (name == VIEW_MAIN) {
            this.onMainLoadStateChanged(true)
        }

        this.triggerLoginIfRequired()
    }

    /*
     * Update state when there is a load error, which may trigger a login redirect once all views are loaded
     */
    fun onViewLoadFailed(name: String, error: UIError) {

        views[name] = true

        if (error.errorCode.equals(ErrorCodes.loginRequired)) {
            this.loginRequired = true
        }

        this.triggerLoginIfRequired()
    }

    /*
     * Reset state when the Reload Data button is clicked
     */
    fun clearState() {

        for (view in this.views) {
            view.setValue(false)
        }

        this.loginRequired = false
    }

    /*
     * If all views are loaded and one or more has reported login required, then trigger a redirect
     */
    private fun triggerLoginIfRequired() {

        val allViewsLoaded = this.views.filter { i -> i.value }.size == this.views.size
        if (allViewsLoaded && this.loginRequired) {
            this.onLoginRequiredAction()
        }
    }
}
