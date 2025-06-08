package com.authsamples.finalmobileapp.app

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.authsamples.finalmobileapp.api.client.FetchCache
import com.authsamples.finalmobileapp.api.client.FetchClient
import com.authsamples.finalmobileapp.configuration.Configuration
import com.authsamples.finalmobileapp.configuration.ConfigurationLoader
import com.authsamples.finalmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.finalmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authsamples.finalmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.finalmobileapp.plumbing.oauth.OAuthClient
import com.authsamples.finalmobileapp.plumbing.oauth.OAuthClientImpl
import com.authsamples.finalmobileapp.views.companies.CompaniesViewModel
import com.authsamples.finalmobileapp.views.transactions.TransactionsViewModel
import com.authsamples.finalmobileapp.views.userinfo.UserInfoViewModel
import com.authsamples.finalmobileapp.views.utilities.DeviceSecurity
import com.authsamples.finalmobileapp.views.utilities.ViewModelCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/*
 * Global data is stored in the view model class for the main activity, which is created only once
 */
class MainActivityViewModel(private val app: Application) : AndroidViewModel(app) {

    // Global objects
    val configuration: Configuration
    val oauthClient: OAuthClient
    val fetchClient: FetchClient
    val viewModelCoordinator: ViewModelCoordinator

    // Other infrastructure
    val fetchCache: FetchCache
    val eventBus: EventBus

    // State
    var isLoaded: Boolean
    var isTopMost: Boolean
    var isDeviceSecured: Boolean

    // Child view models
    private var companiesViewModel: CompaniesViewModel?
    private var transactionsViewModel: TransactionsViewModel?
    private var userInfoViewModel: UserInfoViewModel?

    // Observable data
    var error: MutableState<UIError?> = mutableStateOf(null)

    init {

        // Load configuration from the deployed JSON file
        this.configuration = ConfigurationLoader().load(this.app.applicationContext)

        // Create objects used for coordination
        this.fetchCache = FetchCache()
        this.eventBus = EventBus.getDefault()

        // Create global objects for OAuth and API calls
        this.oauthClient = OAuthClientImpl(this.configuration.oauth, this.app.applicationContext)
        this.fetchClient = FetchClient(this.configuration, this.fetchCache, this.oauthClient)
        this.viewModelCoordinator = ViewModelCoordinator(this.eventBus, this.fetchCache)

        // Initialize child view models
        this.companiesViewModel = null
        this.transactionsViewModel = null
        this.userInfoViewModel = null

        // Initialize state
        this.isLoaded = false
        this.isTopMost = true
        this.isDeviceSecured = DeviceSecurity.isDeviceSecured(this.app.applicationContext)
    }

    /*
     * Initialization at startup, to load OpenID Connect metadata and any stored tokens
     */
    fun initialize(onSuccess: () -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivityViewModel
            try {

                that.oauthClient.initialize()
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
    fun startLogin(launchAction: (i: Intent) -> Unit) {

        // Prevent re-entrancy
        if (!this.isTopMost) {
            return
        }

        // Reset cached state
        this.viewModelCoordinator.resetState()
        this.updateError(null)

        // Prevent deep links being processed during login
        this.isTopMost = false

        try {

            // Run the redirect on the main thread
            this.oauthClient.startLogin(launchAction)

        } catch (ex: Throwable) {

            // Report errors on the main thread, and ignore expected errors
            this.isTopMost = true
            val uiError = ErrorFactory().fromException(ex)
            if (uiError.errorCode != ErrorCodes.redirectCancelled) {
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
                that.oauthClient.finishLogin(responseIntent)

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
        this.viewModelCoordinator.resetState()
        this.updateError(null)

        // Prevent deep links being processed during logout
        this.isTopMost = false

        try {

            // Run the logout redirect on the main thread
            this.oauthClient.startLogout(launchAction)

        } catch (ex: Throwable) {

            // Only report logout errors to the console
            val uiError = ErrorFactory().fromException(ex)
            if (uiError.errorCode != ErrorCodes.redirectCancelled) {
                ErrorConsoleReporter.output(uiError, app)
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
        this.oauthClient.finishLogout()
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

        if (this.error.value != null || this.viewModelCoordinator.hasErrors()) {
            this.reloadData(false)
        }
    }

    fun getCompaniesViewModel(): CompaniesViewModel {

        if (this.companiesViewModel == null) {

            this.companiesViewModel = CompaniesViewModel(
                this.fetchClient,
                this.eventBus,
                this.viewModelCoordinator
            )
        }

        return this.companiesViewModel!!
    }

    fun getTransactionsViewModel(): TransactionsViewModel {

        if (this.transactionsViewModel == null) {

            this.transactionsViewModel = TransactionsViewModel(
                this.fetchClient,
                this.eventBus,
                this.viewModelCoordinator
            )
        }

        return this.transactionsViewModel!!
    }

    fun getUserInfoViewModel(): UserInfoViewModel {

        if (this.userInfoViewModel == null) {

            this.userInfoViewModel = UserInfoViewModel(
                this.fetchClient,
                this.eventBus,
                this.viewModelCoordinator
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
            this.oauthClient.expireAccessToken()

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
            this.oauthClient.expireRefreshToken()

        } catch (ex: Throwable) {

            val uiError = ErrorFactory().fromException(ex)
            this.updateError(uiError)
        }
    }

    /*
     * Update data and inform the binding system
     */
    fun updateError(error: UIError?) {
        this.error.value = error
    }
}
