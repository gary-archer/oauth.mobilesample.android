package com.authguidance.basicmobileapp.views.activities

import android.app.admin.DevicePolicyManager
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.Application
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.ActivityMainBinding
import com.authguidance.basicmobileapp.plumbing.utilities.NavigationHelper
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.configuration.Configuration
import com.authguidance.basicmobileapp.configuration.ConfigurationLoader
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.oauth.AuthenticatorImpl
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.plumbing.utilities.SecureDevice
import com.authguidance.basicmobileapp.views.ViewManager
import com.authguidance.basicmobileapp.views.fragments.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.fragments.HeaderButtonsFragment
import com.authguidance.basicmobileapp.views.fragments.SessionFragment
import com.authguidance.basicmobileapp.views.fragments.TitleFragment
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

    // View settings
    lateinit var viewManager: ViewManager
    var isTopMost: Boolean = true

    // Global state
    lateinit var configuration: Configuration
    lateinit var authenticator: AuthenticatorImpl
    lateinit var apiClient: ApiClient

    /*
     * Create the activity in a safe manner, to set up navigation and data binding
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Set up application state
        this.app().setMainActivity(this)

        // Set up data binding
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Set up navigation, which will point to the blank fragment until the device is secured
        this.navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navController = this.navHostFragment.navController

        // Before running the app, force the device to be secured
        if (!SecureDevice.isSecured(this)) {

            // If not then force a lock screen to be set
            this.isTopMost = false
            this.openLockScreenSettings()
        } else {

            // Otherwise run the application startup logic
            this.startApp()
        }
    }

    /*
     * Handle invoking the lock screen
     */
    private fun openLockScreenSettings() {

        // When the user selects the Settings option, an intent and open Lock Screen Settings
        val agreeCallback = {
            val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
            this.startActivityForResult(intent, Constants.SET_LOCK_SCREEN_REQUEST_CODE)
        }

        // When the user selects the Exit option, exit the Android App
        val declineCallback = {
            this.finishAndRemoveTask()
        }

        // Invoke the dialog to prompt the user
        SecureDevice.forceLockScreenUpdate(this, agreeCallback, declineCallback)
    }

    /*
     * Run the app startup and then the initial navigation
     */
    private fun startApp() {

        if (this.initialiseApp()) {

            // Then navigate to the start fragment
            if (NavigationHelper().isDeepLinkIntent(this.intent)) {

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
    }

    /*
     * Do the application initialization
     */
    private fun initialiseApp(): Boolean {

        try {

            // Initialise error state
            val errorFragment = this.supportFragmentManager.findFragmentById(R.id.mainErrorSummaryFragment) as ErrorSummaryFragment
            errorFragment.clearError()

            // Load configuration
            this.configuration = ConfigurationLoader().load(this.applicationContext)

            // Create the authenticator
            this.authenticator = AuthenticatorImpl(this.configuration.oauth, this.applicationContext)
            this.apiClient = ApiClient(this.configuration.app.apiBaseUrl, this.authenticator)

            // Create the view manager
            this.viewManager = ViewManager(this::onLoadStateChanged, this::onLoginRequired)

            // Ask the title fragment to load user info
            val titleFragment = this.supportFragmentManager.findFragmentById(R.id.titleFragment) as TitleFragment
            titleFragment.loadUserInfo()

            // Show the API session id
            val sessionFragment = this.supportFragmentManager.findFragmentById(R.id.sessionFragment) as SessionFragment
            sessionFragment.show()
            return true
        } catch (ex: Throwable) {

            // Display the startup error details
            this.handleException(ex)
            return false
        }
    }

    /*
     * Handle the result from the lock screen system activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // See if this is a response from the lock screen
        if (requestCode == Constants.SET_LOCK_SCREEN_REQUEST_CODE) {
            if (!SecureDevice.isSecured(this)) {

                // Exit the app if still not secured
                this.finishAndRemoveTask()
            } else {

                // Start the app now that the device is secure
                this.isTopMost = true
                this.startApp()
            }
        }

        // Handle login responses and reset state
        else if (requestCode == Constants.LOGIN_REDIRECT_REQUEST_CODE) {
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
     * Update session buttons when the main view starts and ends loading
     */
    private fun onLoadStateChanged(loaded: Boolean) {

        val buttonFragment = this.supportFragmentManager.findFragmentById(R.id.buttonHeaderFragment) as HeaderButtonsFragment
        buttonFragment.setButtonEnabledState(loaded)
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

        // If there is an error then reinitialise the app to force all fragments to reload
        val mainErrorFragment = this.supportFragmentManager.findFragmentById(R.id.mainErrorSummaryFragment) as ErrorSummaryFragment
        if (mainErrorFragment.hasError() || this.viewManager.hasError()) {
            if (!this.initialiseApp()) {
                return
            }
        }

        // Move to the home view
        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.companiesFragment
        )
    }

    /*
     * Access the application in a typed manner
     */
    fun app(): Application {
        return this.application as Application
    }

    /*
     * Start the login redirect
     */
    private fun startLogin() {

        if (this.isTopMost) {

            // Prevent re-entrancy if 2 fragments get a 401 at once and both try to force a login
            this.isTopMost = false
            CoroutineScope(Dispatchers.IO).launch {

                val that = this@MainActivity
                try {

                    // Start the redirect
                    that.authenticator.startLogin(that, Constants.LOGIN_REDIRECT_REQUEST_CODE)
                } catch (ex: Throwable) {

                    // Report errors such as those looking up endpoints
                    withContext(Dispatchers.Main) {
                        that.isTopMost = true
                        that.handleException(ex)
                    }
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
                that.authenticator.finishLogin(loginResponseIntent)

                withContext(Dispatchers.Main) {

                    // Show the API session id
                    val sessionFragment = that.supportFragmentManager.findFragmentById(R.id.sessionFragment) as SessionFragment
                    sessionFragment.show()

                    // Reload data when returning from login
                    EventBus.getDefault().post(ReloadEvent(false))
                }
            } catch (ex: Throwable) {

                // Report errors such as those processing the authorization code grant
                withContext(Dispatchers.Main) {
                    that.handleException(ex)
                }
            }
        }
    }

    /*
     * Update token storage to make the access token act like it is expired
     */
    fun expireAccessToken() {
        this.authenticator.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    fun expireRefreshToken() {
        this.authenticator.expireRefreshToken()
    }

    /*
     * Remove tokens and navigate to login required
     */
    fun startLogout() {

        try {

            // Trigger the logout process, which will remove tokens and redirect to clear the OAuth session cookie
            this.isTopMost = false
            this.authenticator.startLogout(this, Constants.LOGOUT_REDIRECT_REQUEST_CODE)
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

        // Free resources
        this.authenticator.finishLogout()

        // Update the UI to clear user info after logging out
        val titleFragment = this.supportFragmentManager.findFragmentById(R.id.titleFragment) as TitleFragment
        titleFragment.clearUserInfo()

        // Clear the API session id when logged out
        val sessionFragment = this.supportFragmentManager.findFragmentById(R.id.sessionFragment) as SessionFragment
        sessionFragment.clear()

        // Disable session buttons after logout
        this.onLoadStateChanged(false)

        // Move to the login required page
        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.loginRequiredFragment)
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    fun handleException(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if (error.errorCode == ErrorCodes.loginCancelled) {

            // If the user has closed the Chrome Custom Tab without logging in, move to the Login Required view
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.loginRequiredFragment)

            // Clear the API session id when logged out
            val sessionFragment = this.supportFragmentManager.findFragmentById(R.id.sessionFragment) as SessionFragment
            sessionFragment.clear()
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
     * Clean up resources when destroyed, which occurs after the screen orientation is changed
     */
    override fun onDestroy() {
        super.onDestroy()
        this.app().setMainActivity(null)
    }
}
