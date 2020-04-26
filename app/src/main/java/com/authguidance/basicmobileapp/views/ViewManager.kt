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

    // Flags used to update header button state
    private var mainViewLoaded: Boolean = false
    private var userInfoLoaded: Boolean = false

    // View errors when calling the API
    private var mainViewLoadError: UIError? = null
    private var userInfoLoadError: UIError? = null

    /*
     * Handle the main view loading event
     */
    fun onMainViewLoading() {
        this.onLoadStateChanged(false)
    }

    /*
     * Handle the main view loaded event
     */
    fun onMainViewLoaded() {
        this.mainViewLoaded = true
        this.mainViewLoadError = null
        this.onLoadStateChanged(true)
    }

    /*
     * Handle the main view load failed event
     */
    fun onMainViewLoadFailed(error: UIError) {
        this.mainViewLoaded = true
        this.mainViewLoadError = error
        this.triggerLoginIfRequired()
    }

    /*
     * After a successful user info load, reset error state
     */
    fun onUserInfoLoaded() {
        this.userInfoLoaded = true
        this.userInfoLoadError = null
    }

    /*
     * After a failed user info load, store the error
     */
    fun onUserInfoLoadFailed(error: UIError) {
        this.userInfoLoaded = true
        this.userInfoLoadError = error
        this.triggerLoginIfRequired()
    }

    /*
     * Indicate if there is an error
     */
    fun hasError(): Boolean {

        val mainError = this.mainViewLoadError
        val userError = this.userInfoLoadError

        if ((mainError != null && mainError.errorCode != ErrorCodes.loginRequired) ||
            (userError != null && userError.errorCode != ErrorCodes.loginRequired)) {
            return true
        }

        return false
    }

    /*
     * Wait for both the main view and user info to load, then trigger a login redirect
     */
    private fun triggerLoginIfRequired() {

        val mainError = this.mainViewLoadError
        val userError = this.userInfoLoadError

        // First check both views are loaded
        if (this.mainViewLoaded && this.userInfoLoaded) {

            // Next check if there is one or more login required errors
            if ((mainError != null && mainError.errorCode.equals(ErrorCodes.loginRequired)) ||
                (userError != null && userError.errorCode.equals(ErrorCodes.loginRequired))) {

                // First reset to prevent leftover data problems later
                this.mainViewLoaded = false
                this.userInfoLoaded = false
                this.mainViewLoadError = null
                this.userInfoLoadError = null

                // If so then ask the parent to trigger a login redirect
                this.onLoginRequired()
            }
        }
    }
}
