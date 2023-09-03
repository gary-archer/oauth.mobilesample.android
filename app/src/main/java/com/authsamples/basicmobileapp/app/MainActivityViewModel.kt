package com.authsamples.basicmobileapp.app

import android.app.Application
import android.content.Intent
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.FetchCache
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.configuration.Configuration
import com.authsamples.basicmobileapp.configuration.ConfigurationLoader
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.authsamples.basicmobileapp.plumbing.oauth.AuthenticatorImpl
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.utilities.DeviceSecurity
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * Global data is stored in the view model class for the main activity, which is created only once
 */
class MainActivityViewModel(val app: Application) : AndroidViewModel(app), Observable {

    // Global objects
    val configuration: Configuration
    val authenticator: Authenticator
    val fetchClient: FetchClient
    val fetchCache: FetchCache
    val viewModelCoordinator: ViewModelCoordinator
    var isDeviceSecured: Boolean = false
    var isTopMost: Boolean = true

    // Observable data
    var error: UIError? = null
    private val callbacks = PropertyChangeRegistry()

    init {

        // Load configuration from the deployed JSON file
        this.configuration = ConfigurationLoader().load(this.app.applicationContext)

        // Create global objects for OAuth and API calls
        this.authenticator = AuthenticatorImpl(this.configuration.oauth, this.app.applicationContext)
        this.fetchClient = FetchClient(this.configuration, this.authenticator)
        this.fetchCache = FetchCache()

        // Create a helper class notified by view models that call APIs
        // This is used to trigger a login redirect only once, after all view models have tried to load
        this.viewModelCoordinator = ViewModelCoordinator(this.fetchCache)

        // Initialize flags
        this.isDeviceSecured = DeviceSecurity.isDeviceSecured(this.app.applicationContext)
        this.isTopMost = true
    }

    /*
     * Start a login operation to run the authorization redirect
     */
    fun startLogin(launchAction: (i: Intent) -> Unit, onCancelled: () -> Unit) {

        // Prevent re-entrancy
        if (!this.isTopMost) {
            return
        }

        // Reset cached state
        this.fetchCache.clearAll()
        this.viewModelCoordinator.resetState()
        this.updateError(null)

        // Prevent deep links being processed during login
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

                // Report errors on the main thread, but ignore expected errors
                withContext(Dispatchers.Main) {
                    that.isTopMost = true

                    val uiError = ErrorFactory().fromException(ex)
                    if (uiError.errorCode == ErrorCodes.redirectCancelled) {
                        onCancelled()
                    } else {
                        that.updateError(uiError)
                    }
                }
            }
        }
    }

    /*
     * Complete login processing after the post login page executes
     */
    fun finishLogin(
        responseIntent: Intent?,
        onSuccess: () -> Unit,
        onCancelled: () -> Unit
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

                // Report errors on the main thread, but ignore expected errors
                withContext(Dispatchers.Main) {

                    val uiError = ErrorFactory().fromException(ex)
                    if (uiError.errorCode == ErrorCodes.redirectCancelled) {
                        onCancelled()
                    } else {
                        that.updateError(uiError)
                    }
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
     * Run a logout redirect
     */
    fun startLogout(launchAction: (i: Intent) -> Unit, onError: () -> Unit) {

        // Prevent re-entrancy
        if (!this.isTopMost) {
            return
        }

        // Reset cached state
        this.fetchCache.clearAll()
        this.viewModelCoordinator.resetState()
        this.updateError(null)

        // Prevent deep links being processed during logout
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

                    // Only report logout errors to the console
                    val uiError = ErrorFactory().fromException(ex)
                    if (uiError.errorCode != ErrorCodes.redirectCancelled) {
                        ErrorConsoleReporter.output(uiError, that.app)
                    }

                    // Then free resources and notify the caller
                    that.finishLogout()
                    onError()
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

    /*
     * For testing, make the access token act expired and handle any errors
     */
    fun expireAccessToken() {
        try {

            this.updateError(null)
            this.authenticator.expireAccessToken()

        } catch (ex: Throwable) {

            val uiError = ErrorFactory().fromException(ex)
            this.updateError(uiError)
        }
    }

    /*
     * For testing, make the refresh token act expired and handle any errors
     */
    fun expireRefreshToken() {
        try {

            this.updateError(null)
            this.authenticator.expireRefreshToken()

        } catch (ex: Throwable) {

            val uiError = ErrorFactory().fromException(ex)
            this.updateError(uiError)
        }
    }

    /*
     * Data to pass when invoking the child error summary view
     */
    fun errorSummaryViewModel(): ErrorSummaryViewModelData {

        return ErrorSummaryViewModelData(
            hyperlinkText = app.getString(R.string.main_error_hyperlink),
            dialogTitle = app.getString(R.string.main_error_dialogtitle),
            error = this.error
        )
    }

    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.remove(callback)
    }

    /*
     * Update data and inform the binding system
     */
    fun updateError(error: UIError?) {
        this.error = error
        this.callbacks.notifyCallbacks(this, 0, null)
    }
}
