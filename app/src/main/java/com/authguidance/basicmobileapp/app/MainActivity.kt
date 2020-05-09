package com.authguidance.basicmobileapp.app

import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.databinding.ActivityMainBinding
import com.authguidance.basicmobileapp.plumbing.utilities.NavigationHelper
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.events.InitialLoadEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.views.utilities.ViewManager
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.headings.HeaderButtonsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/*
 * Our Single Activity App's activity
 */
class MainActivity : AppCompatActivity() {

    // Navigation related fields
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment

    // Properties
    private var model: MainActivityViewModel
    var viewManager: ViewManager
    var isTopMost: Boolean

    /*
     * Instance creation logic
     */
    init {

        // Create the model, which we'll initialise later
        this.model = MainActivityViewModel()
        this.isTopMost = true

        // Initialise the view manager
        this.viewManager =
            ViewManager(
                this::onLoadStateChanged,
                this::onLoginRequired
            )
        this.viewManager.setViewCount(2)
    }

    /*
     * Create the activity in a safe manner, to set up navigation and data binding
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Set up navigation and data binding
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navController = this.navHostFragment.navController

        // Try to initialise the application
        this.app().setMainActivity(this)
        this.initialiseApp()
    }

    /*
     * Initialise the app and handle errors
     */
    private fun initialiseApp() {

        try {
            // Initialise the model
            this.model.initialise(this.applicationContext)

            // Send an initial load event to views that run at startup
            EventBus.getDefault().post(InitialLoadEvent())

            // Do the initial navigate operation to populate the main view
            this.runInitialNavigation()
        } catch (ex: Throwable) {

            // Display the startup error details
            this.handleException(ex)
        }
    }

    /*
     * Run the app startup and navigate to the start fragment
     */
    private fun runInitialNavigation() {

        if (!this.model.isDeviceSecured) {

            // If the device is not secured we will move to a view that prompts the user to do so
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.deviceNotSecuredFragment
            )
        } else if (NavigationHelper().isDeepLinkIntent(this.intent)) {

            // If there was a deep link then follow it
            NavigationHelper().navigateToDeepLink(
                this.intent,
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment
            )
        } else {

            // Otherwise move home
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.companiesFragment
            )
        }
    }

    /*
     * Handle the result from other activities, such as AppAuth or lock screen activities
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // Handle login responses and reset state
        if (requestCode == Constants.LOGIN_REDIRECT_REQUEST_CODE) {
            if (data != null) {
                this.isTopMost = true
                this.finishLogin(data)
            }
        }

        // Handle logout responses and reset state
        else if (requestCode == Constants.LOGOUT_REDIRECT_REQUEST_CODE) {
            this.isTopMost = true
            this.finishLogout()
        }
    }

    /*
     * Handle deep links while the app is running
     */
    override fun onNewIntent(receivedIntent: Intent?) {

        super.onNewIntent(receivedIntent)

        if (NavigationHelper().isDeepLinkIntent(receivedIntent)) {
            NavigationHelper().navigateToDeepLink(
                receivedIntent,
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment
            )
        }
    }

    /*
     * Update the load state and session buttons during and after view load
     */
    private fun onLoadStateChanged(loaded: Boolean) {

        // Update our state
        this.model.isDataLoaded = loaded

        // Ask the header buttons fragment to update
        val buttonFragment = this.supportFragmentManager.findFragmentById(R.id.buttonHeaderFragment) as HeaderButtonsFragment
        buttonFragment.update()
    }

    /*
     * Start a login redirect when the view manager informs us that a permanent 401 has occurred
     */
    private fun onLoginRequired() {
        this.startLogin()
    }

    /*
     * Handle home navigation
     */
    fun onHome() {

        // If we are not initialised, then support retrying by reinitialising the app
        if (!this.model.isInitialised) {
            this.initialiseApp()
        }

        if (this.model.isInitialised) {

            // Move to the home view
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.companiesFragment
            )

            // If there is an error loading data from the API then force a reload
            if (this.model.authenticator!!.isLoggedIn() && !this.model.isDataLoaded) {
                this.onReloadData(false)
            }
        }
    }

    /*
     * Start the login redirect
     */
    private fun startLogin() {

        this.isTopMost = false
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivity
            try {

                // Start the redirect
                that.model.authenticator!!.startLogin(that, Constants.LOGIN_REDIRECT_REQUEST_CODE)
            } catch (ex: Throwable) {

                // Report errors such as those looking up endpoints
                withContext(Dispatchers.Main) {
                    that.isTopMost = true
                    that.handleException(ex)
                }
            }
        }
    }

    /*
     * After the post login page executes, we receive the login response here
     */
    private fun finishLogin(loginResponseIntent: Intent) {

        // Handle completion after login completion, which could be a success or failure response
        CoroutineScope(Dispatchers.IO).launch {

            // Switch to a background thread
            val that = this@MainActivity
            try {
                // Handle completion after login success, which will exchange the authorization code for tokens
                that.model.authenticator!!.finishLogin(loginResponseIntent)

                withContext(Dispatchers.Main) {

                    // Reload data after logging in
                    that.onReloadData(false)
                }
            } catch (ex: Throwable) {

                // Report errors such as those processing the authorization code grant
                withContext(Dispatchers.Main) {
                    that.handleException(ex)
                }
            } finally {
                that.isTopMost = false
            }
        }
    }

    /*
     * Remove tokens and navigate to login required
     */
    fun onStartLogout() {

        try {

            // Trigger the logout process, which will remove tokens and redirect to clear the OAuth session cookie
            this.isTopMost = false
            this.model.authenticator!!.startLogout(this, Constants.LOGOUT_REDIRECT_REQUEST_CODE)
        } catch (ex: Throwable) {

            // Report errors such as those looking up endpoints
            this.isTopMost = true
            this.handleException(ex)
        }
    }

    /*
     * Free resources when we receive the logout response
     */
    private fun finishLogout() {

        // Finish processing
        this.model.authenticator!!.finishLogout()

        // Update state
        this.onLoadStateChanged(false)
        this.isTopMost = false

        // Move to the login required page
        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.loginRequiredFragment)

        // Send an event to fragments
        EventBus.getDefault().post(UnloadEvent())
    }

    /*
     * Publish an event to update all active views
     */
    fun onReloadData(causeError: Boolean) {

        this.viewManager.setViewCount(2)
        EventBus.getDefault().post(ReloadEvent(causeError))
    }

    /*
     * Indicate whether in the login required view
     */
    fun isInLoginRequired(): Boolean {
        return false
    }

    /*
     * Update token storage to make the access token act like it is expired
     */
    fun onExpireAccessToken() {
        this.model.authenticator!!.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    fun onExpireRefreshToken() {
        this.model.authenticator!!.expireRefreshToken()
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    fun handleException(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if (error.errorCode.equals(ErrorCodes.loginCancelled)) {

            // If the user has closed the Chrome Custom Tab without logging in, move to the Login Required view
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.loginRequiredFragment)
        } else {

            // Otherwise there is a technical error and we display summary details
            val errorFragment = this.supportFragmentManager.findFragmentById(R.id.mainErrorSummaryFragment) as ErrorSummaryFragment
            errorFragment.reportError(
                this.getString(R.string.main_error_hyperlink),
                this.getString(R.string.main_error_dialogtitle),
                error)
        }
    }

    /*
     * Access the application in a typed manner
     */
    fun app(): Application {
        return this.application as Application
    }

    /*
     * Clean up resources when destroyed, which occurs after the screen orientation is changed
     */
    override fun onDestroy() {
        super.onDestroy()
        this.app().setMainActivity(null)
    }

    /*
     * An accessor used by child fragments that call APIs
     */
    fun getApiClient(): ApiClient? {
        return this.model.apiClient
    }

    /*
     * Inform header buttons whether to set their state to enabled or disabled
     */
    fun isDataLoaded(): Boolean {
        return this.model.isDataLoaded
    }

    /*
     * Inform the session fragment whether to show its session id
     */
    fun shouldShowSessionId(): Boolean {
        return this.model.isInitialised && this.model.isDeviceSecured && this.model.authenticator!!.isLoggedIn()
    }

    /*
     * Inform the user info view whether to load user info from the API for the logged in user
     */
    fun shouldLoadUserInfo(): Boolean {
        return this.model.isInitialised && this.model.isDeviceSecured && !this.isInLoginRequired()
    }
}
