package com.authguidance.basicmobileapp.app

import android.content.Context
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.configuration.Configuration
import com.authguidance.basicmobileapp.configuration.ConfigurationLoader
import com.authguidance.basicmobileapp.plumbing.oauth.AuthenticatorImpl
import com.authguidance.basicmobileapp.plumbing.utilities.DeviceSecurity

/*
 * A primitive view model class to contain global objects and state
 */
class MainActivityViewModel {

    // Global objects used by the main activity
    var configuration: Configuration? = null
    var authenticator: AuthenticatorImpl? = null
    var apiClient: ApiClient? = null

    // State used by the main activity
    var isInitialised: Boolean = false
    var isDeviceSecured: Boolean = false
    var isDataLoaded: Boolean = false

    /*
     * Initialise the model after it has been created
     */
    fun initialise(context: Context) {

        // Reset state flags
        this.isInitialised = false
        this.isDeviceSecured = DeviceSecurity.isDeviceSecured(context)
        this.isDataLoaded = false

        // Load configuration
        this.configuration = ConfigurationLoader().load(context)

        // Create the authenticator
        this.authenticator = AuthenticatorImpl(this.configuration!!.oauth, context)
        this.apiClient = ApiClient(this.configuration!!.app.apiBaseUrl, this.authenticator!!)

        // Indicate successful startup
        this.isInitialised = true
    }
}