package com.authguidance.basicmobileapp

import android.content.Context
import com.authguidance.basicmobileapp.configuration.Configuration
import com.authguidance.basicmobileapp.logic.utilities.ConfigurationLoader
import com.authguidance.basicmobileapp.plumbing.oauth.Authenticator

/*
 * A holder for application state, rather than storing in our main activity class, which can get recreated
 */
class ApplicationState(val applicationContext: Context) {

    // Global state
    lateinit var configuration: Configuration
    lateinit var authenticator: Authenticator

    // Flags
    var isLoaded = false
    var isMainActivityTopMost = true

    /*
     * Load application state when first called
     */
    fun load() {

        if(!isLoaded) {

            // Load our JSON configuration
            this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)

            // Create the global authenticator
            this.authenticator = Authenticator(this.configuration.oauth, this.applicationContext)

            // Prevent re-entrancy
            isLoaded = true
        }
    }
}
