package com.authguidance.basicmobileapp.app

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.configuration.Configuration
import com.authguidance.basicmobileapp.configuration.ConfigurationLoader
import com.authguidance.basicmobileapp.plumbing.oauth.AuthenticatorImpl
import com.authguidance.basicmobileapp.views.utilities.ApiViewEvents
import com.authguidance.basicmobileapp.views.utilities.Constants
import com.authguidance.basicmobileapp.views.utilities.DeviceSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * The view model class for the main activity
 */
class MainActivityViewModel(
    private val app: Application,
    private val activityEvents: MainActivityEvents
) : ViewModel() {

    // Global objects used by the main activity
    var configuration: Configuration
    var authenticator: AuthenticatorImpl
    var apiClient: ApiClient
    var apiViewEvents: ApiViewEvents

    // State used by the main activity
    var isDeviceSecured: Boolean = false
    var isMainViewLoaded: Boolean = false
    var isTopMost: Boolean = true

    init {

        // Load configuration from the deployed JSON file
        this.configuration = ConfigurationLoader().load(this.app.applicationContext)

        // Create global objects for OAuth and API calls
        this.authenticator = AuthenticatorImpl(this.configuration.oauth, this.app.applicationContext)
        this.apiClient = ApiClient(this.configuration.app.apiBaseUrl, this.authenticator)

        // Initialize flags
        this.isDeviceSecured = DeviceSecurity.isDeviceSecured(this.app.applicationContext)
        this.isMainViewLoaded = false
        this.isTopMost = true

        // Create a helper class to notify us about views that make API calls
        // This will enable us to only trigger a login redirect once, after all views have tried to load
        this.apiViewEvents = ApiViewEvents(
            this.activityEvents::onLoginRequired,
            this.activityEvents::onMainLoadStateChanged,
        )
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

        // Run on the UI thread since we present UI elements
        CoroutineScope(Dispatchers.Main).launch {

            val that = this@MainActivityViewModel
            try {

                // Start the redirect
                that.authenticator.startLogin(launchAction)

            } catch (ex: Throwable) {

                // Report errors such as those looking up endpoints
                that.isTopMost = true
                onError(ex)
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

        // Switch to a background thread to perform the code exchange
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
                that.isTopMost = true
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

        // Run on the UI thread since we present UI elements
        CoroutineScope(Dispatchers.Main).launch {

            val that = this@MainActivityViewModel
            try {

                // Trigger the logout process, which will remove tokens and redirect to clear the OAuth session cookie
                that.authenticator.startLogout(launchAction)

            } catch (ex: Throwable) {
                onError(ex)
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

    /*
     * Update token storage to make the access token act like it is expired
     */
    fun onExpireAccessToken() {
        this.authenticator.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    fun onExpireRefreshToken() {
        this.authenticator.expireRefreshToken()
    }
}
