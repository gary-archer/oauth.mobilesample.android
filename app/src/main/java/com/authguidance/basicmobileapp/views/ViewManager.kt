package com.authguidance.basicmobileapp.views

import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError

/*
 * A helper class to coordinate multiple views
 */
class ViewManager(
    private val onLoadStateChanged: (loaded: Boolean) -> Unit,
    private val onLoginRequired: () -> Unit) {

    // Flags
    private var mainViewLoaded: Boolean = false
    private var userInfoLoaded: Boolean = false

    // Errors when calling the API
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
        this.onLoadStateChanged(true)
    }

    /*
     * Handle the main view load failed event
     */
    fun onMainViewLoadFailed() {
        this.mainViewLoaded = true
        this.triggerLoginIfRequired();
    }

    /*
     * After a successful user info load, reset error state
     */
    fun onUserInfoLoaded() {
        this.userInfoLoaded = true
    }

    /*
     * After a failed user info load, store the error
     */
    fun onUserInfoLoadFailed() {
        this.userInfoLoaded = true
        this.triggerLoginIfRequired();
    }

    /*
     * Indicate if there is an error
     */
    fun hasError(): Boolean {

        if( (this.mainViewLoadError != null && this.mainViewLoadError!!.errorCode != ErrorCodes.loginRequired) ||
            (this.userInfoLoadError != null && this.userInfoLoadError!!.errorCode != ErrorCodes.loginRequired)) {
            return true
        }

        return false
    }

    /*
     * Wait for both the main view and user info to load, then trigger a login redirect
     */
    private fun triggerLoginIfRequired() {

        // First check both views are loaded
        if (this.mainViewLoaded && this.userInfoLoaded) {

            // Next check if there is one or more login required errors
            if ((this.mainViewLoadError != null && this.mainViewLoadError!!.errorCode === ErrorCodes.loginRequired) ||
                (this.userInfoLoadError != null && this.userInfoLoadError!!.errorCode === ErrorCodes.loginRequired)) {

                // If so then ask the parent to trigger a login redirect
                this.onLoginRequired();
            }
        }
    }
}
