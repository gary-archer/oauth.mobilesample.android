package com.authguidance.basicmobileapp

import android.content.Context
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.configuration.Configuration
import com.authguidance.basicmobileapp.configuration.ConfigurationLoader
import com.authguidance.basicmobileapp.plumbing.oauth.Authenticator

/*
 * A holder for application state, rather than storing in our main activity class, which can get recreated
 */
class ApplicationState(val applicationContext: Context) {

    // Global state
    lateinit var configuration: Configuration
    lateinit var authenticator: Authenticator
    lateinit var apiClient: ApiClient

    // Flags
    var isLoaded = false
    var isMainActivityTopMost = true

    /*
     * Load application state when first called
     */
    fun load() {

        if(!isLoaded) {

            // Load our JSON configuration
            this.configuration = ConfigurationLoader()
                .loadConfiguration(this.applicationContext)

            // Create the global authenticator
            this.authenticator = Authenticator(this.configuration.oauth, this.applicationContext)
            this.apiClient = ApiClient(this.configuration.app.apiBaseUrl, this.authenticator)

            // Prevent re-entrancy
            isLoaded = true
        }
    }
}
