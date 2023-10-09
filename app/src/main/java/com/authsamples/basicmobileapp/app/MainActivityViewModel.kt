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
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.authsamples.basicmobileapp.plumbing.oauth.AuthenticatorImpl
import com.authsamples.basicmobileapp.views.companies.CompaniesViewModel
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.transactions.TransactionsViewModel
import com.authsamples.basicmobileapp.views.userinfo.UserInfoViewModel
import com.authsamples.basicmobileapp.views.utilities.DeviceSecurity
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/*
 * Global data is stored in the view model class for the main activity, which is created only once
 */
@Suppress("TooManyFunctions")
class MainActivityViewModel(val app: Application) : AndroidViewModel(app), Observable {

    // Global objects
    val configuration: Configuration
    val authenticator: Authenticator
    val fetchClient: FetchClient

    // Other infrastructure
    val fetchCache: FetchCache
    val eventBus: EventBus
    val viewModelCoordinator: ViewModelCoordinator

    // State
    var isLoaded: Boolean
    var isTopMost: Boolean
    var isDeviceSecured: Boolean

    // Child view models
    private var companiesViewModel: CompaniesViewModel?
    private var transactionsViewModel: TransactionsViewModel?
    private var userInfoViewModel: UserInfoViewModel?

    // Observable data
    var error: UIError?
    private val callbacks = PropertyChangeRegistry()

    init {

        // Load configuration from the deployed JSON file
        this.configuration = ConfigurationLoader().load(this.app.applicationContext)

        // Create objects used for coordination
        this.fetchCache = FetchCache()
        this.eventBus = EventBus.getDefault()
        this.viewModelCoordinator = ViewModelCoordinator(this.eventBus, this.fetchCache)

        // Create global objects for OAuth and API calls
        this.authenticator = AuthenticatorImpl(this.configuration.oauth, this.app.applicationContext)
        this.fetchClient = FetchClient(this.configuration, this.fetchCache, this.authenticator)

        // Initialize child view models
        this.companiesViewModel = null
        this.transactionsViewModel = null
        this.userInfoViewModel = null

        // Initialize state
        this.isLoaded = false
        this.isTopMost = true
        this.isDeviceSecured = DeviceSecurity.isDeviceSecured(this.app.applicationContext)
        this.error = null
    }

    /*
     * Initialization at startup, to load OpenID Connect metadata and any stored tokens
     */
    fun initialize(onSuccess: () -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivityViewModel
            try {

                that.authenticator.initialize()
                withContext(Dispatchers.Main) {
                    that.isLoaded = true
                    onSuccess()
                }

            } catch (ex: Throwable) {

                // Report errors on the main thread, but ignore expected errors
                withContext(Dispatchers.Main) {
                    that.updateError(ErrorFactory().fromException(ex))
                }
            }
        }
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

        try {

            // Run the redirect on the main thread
            this.authenticator.startLogin(launchAction)

        } catch (ex: Throwable) {

            // Report errors on the main thread, and ignore expected errors
            this.isTopMost = true
            val uiError = ErrorFactory().fromException(ex)
            if (uiError.errorCode == ErrorCodes.redirectCancelled) {
                onCancelled()
            } else {
                this.updateError(uiError)
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

        try {

            // Run the logout redirect on the main thread
            this.authenticator.startLogout(launchAction)

        } catch (ex: Throwable) {

            // Only report logout errors to the console
            val uiError = ErrorFactory().fromException(ex)
            if (uiError.errorCode != ErrorCodes.redirectCancelled) {
                ErrorConsoleReporter.output(uiError, this.app)
            }

            // Then free resources and notify the caller
            this.finishLogout()
            onError()
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
     * Publish an event to update all active views
     */
    fun reloadData(causeError: Boolean) {

        this.updateError(null)
        this.viewModelCoordinator.resetState()
        this.eventBus.post(ReloadDataEvent(causeError))
    }

    /*
     * If there were load errors, try to reload data when Home is pressed
     */
    fun reloadDataOnError() {

        if (this.error != null || this.viewModelCoordinator.hasErrors()) {
            this.reloadData(false)
        }
    }

    fun getCompaniesViewModel(): CompaniesViewModel {

        if (this.companiesViewModel == null) {

            this.companiesViewModel = CompaniesViewModel(
                this.fetchClient,
                this.eventBus,
                this.viewModelCoordinator,
                this.app
            )
        }

        return this.companiesViewModel!!
    }

    fun getTransactionsViewModel(): TransactionsViewModel {

        if (this.transactionsViewModel == null) {

            this.transactionsViewModel = TransactionsViewModel(
                this.fetchClient,
                this.eventBus,
                this.viewModelCoordinator,
                this.app
            )
        }

        return this.transactionsViewModel!!
    }

    fun getUserInfoViewModel(): UserInfoViewModel {

        if (this.userInfoViewModel == null) {

            this.userInfoViewModel = UserInfoViewModel(
                this.fetchClient,
                this.eventBus,
                this.viewModelCoordinator,
                this.app
            )
        }

        return this.userInfoViewModel!!
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
    fun errorSummaryData(): ErrorSummaryViewModelData {

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
