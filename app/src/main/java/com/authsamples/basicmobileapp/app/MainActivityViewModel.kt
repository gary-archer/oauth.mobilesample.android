package com.authsamples.basicmobileapp.app

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.configuration.Configuration
import com.authsamples.basicmobileapp.configuration.ConfigurationLoader
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.authsamples.basicmobileapp.plumbing.oauth.AuthenticatorImpl
import com.authsamples.basicmobileapp.views.utilities.ApiViewEvents
import com.authsamples.basicmobileapp.views.utilities.Constants
import com.authsamples.basicmobileapp.views.utilities.DeviceSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * Global data is stored in the view model class for the main activity, which is created only once
 */
class MainActivityViewModel(val app: Application) : AndroidViewModel(app) {

    // Global objects used by the main activity
    val configuration: Configuration
    val authenticator: Authenticator
    val apiClient: ApiClient
    val apiViewEvents: ApiViewEvents

    var isDeviceSecured: Boolean = false
    var isTopMost: Boolean = true

    init {

        // Load configuration from the deployed JSON file
        this.configuration = ConfigurationLoader().load(this.app.applicationContext)

        // Create global objects for OAuth and API calls
        this.authenticator = AuthenticatorImpl(this.configuration.oauth, this.app.applicationContext)
        this.apiClient = ApiClient(this.configuration.app.apiBaseUrl, this.authenticator)

        // Initialize flags
        this.isDeviceSecured = DeviceSecurity.isDeviceSecured(this.app.applicationContext)
        this.isTopMost = true

        // Create a helper class to notify us about views that make API calls
        // This will enable us to only trigger a login redirect once, after all views have tried to load
        this.apiViewEvents = ApiViewEvents()
        this.apiViewEvents.addView(Constants.VIEW_MAIN)
        this.apiViewEvents.addView(Constants.VIEW_USERINFO)
    }

    /*
     * Start a login operation
     */
    fun startLogin(launchAction: (i: Intent) -> Unit, onError: (Throwable) -> Unit) {

        // Prevent re-entrancy
        if (!this.isTopMost) {
            return
        }

        // Prevent deep links during login
        this.isTopMost = false

        // Run on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivityViewModel
            try {

                // First get metadata if required
                that.authenticator.getMetadata()

                // Run the redirect on the main thread
                withContext(Dispatchers.Main) {
                    that.authenticator.startLogin(launchAction)
                }

            } catch (ex: Throwable) {

                // Report errors on the main thread
                withContext(Dispatchers.Main) {
                    that.isTopMost = true
                    onError(ex)
                }
            }
        }
    }

    /*
     * After the post login page executes, we receive the login response here
     */
    fun finishLogin(
        responseIntent: Intent?,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {

        if (responseIntent == null) {
            this.isTopMost = true
            return
        }

        // Run on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivityViewModel
            try {

                // Handle completion after login success, which will exchange the authorization code for tokens
                that.authenticator.finishLogin(responseIntent)

                // Reload data after logging in
                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (ex: Throwable) {

                // Report errors on the main thread
                withContext(Dispatchers.Main) {
                    onError(ex)
                }

            } finally {

                // Allow deep links again, once the activity is topmost
                withContext(Dispatchers.Main) {
                    that.isTopMost = true
                }
            }
        }
    }

    /*
     * Start a logout redirect
     */
    fun startLogout(launchAction: (i: Intent) -> Unit, onError: (Throwable) -> Unit) {

        // Prevent re-entrancy
        if (!this.isTopMost) {
            return
        }

        // Prevent deep links while the login window is top most
        this.isTopMost = false

        // Run on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivityViewModel
            try {

                // First get metadata if required
                that.authenticator.getMetadata()

                // Run the logout redirect on the main thread
                withContext(Dispatchers.Main) {
                    that.authenticator.startLogout(launchAction)
                }

            } catch (ex: Throwable) {

                // Report errors on the main thread
                withContext(Dispatchers.Main) {
                    onError(ex)
                }
            }
        }
    }

    /*
     * Update state when a logout completes
     */
    fun finishLogout() {
        this.authenticator.finishLogout()
        this.isTopMost = true
    }
}
