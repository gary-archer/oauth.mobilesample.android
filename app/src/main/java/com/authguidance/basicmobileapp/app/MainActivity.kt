package com.authguidance.basicmobileapp.app

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.ActivityMainBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.events.InitialLoadEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.headings.HeaderButtonsFragment
import com.authguidance.basicmobileapp.views.utilities.Constants
import com.authguidance.basicmobileapp.views.utilities.DeviceSecurity
import com.authguidance.basicmobileapp.views.utilities.NavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/*
 * Our Single Activity App's activity
 */
class MainActivity : AppCompatActivity() {

    // The binding contains our view model
    private lateinit var binding: ActivityMainBinding

    // Navigation properties
    private lateinit var navigationHelper: NavigationHelper

    /*
     * Set up of the Single Activity App's main activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        this.app().setMainActivity(this)

        // Create our view model
        val model = MainActivityViewModel(this::onLoadStateChanged, this::onLoginRequired)

        // Populate the shared view model used by child fragments
        this.createSharedViewModel(model)

        // Inflate the view, which will trigger child fragments to run
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.binding.model = model

        // Initialise the navigation system
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navigationHelper = NavigationHelper(navHostFragment) { model.isDeviceSecured }

        // Finally, do the main application load
        this.initialiseApp()
    }

    /*
     * Create or update a view model with data needed by child fragments
     */
    private fun createSharedViewModel(model: MainActivityViewModel) {

        // Get the model from the Android system, which will be created the first time
        val sharedViewModel: MainActivitySharedViewModel by viewModels()

        // Properties related to fragment data access
        sharedViewModel.apiClientAccessor = model::apiClient
        sharedViewModel.viewManager = model.viewManager

        // Properties passed to the header buttons fragment
        sharedViewModel.isDataLoadedAccessor = model::isDataLoaded
        sharedViewModel.onHome = this::onHome
        sharedViewModel.onReload = this::onReloadData
        sharedViewModel.onExpireAccessToken = this::onExpireAccessToken
        sharedViewModel.onExpireRefreshToken = this::onExpireRefreshToken
        sharedViewModel.onLogout = this::onStartLogout

        // Properties passed to the user info fragment
        sharedViewModel.shouldLoadUserInfoAccessor = {
                    model.isInitialised &&
                    model.isDeviceSecured &&
                    !this.navigationHelper.isInLoginRequired()
        }

        // Properties passed to the session fragment
        sharedViewModel.shouldShowSessionIdAccessor = {
                    model.isInitialised &&
                    model.isDeviceSecured &&
                    model.authenticator!!.isLoggedIn()
        }
    }

    /*
     * Initialise the app and handle errors
     */
    private fun initialiseApp() {

        try {
            // Do the main view model initialisation
            this.binding.model!!.initialise(this.applicationContext)

            // Load the main view
            this.navigateStart()

            // Send an initial load event to other views
            EventBus.getDefault().post(InitialLoadEvent())

        } catch (ex: Throwable) {

            // Display the startup error details
            this.handleError(ex)
        }
    }

    /*
     * Navigate to the start fragment, which is usually the companies view
     */
    private fun navigateStart() {

        val model = this.binding.model!!

        if (!model.isDeviceSecured) {

            // If the device is not secured we will move to a view that prompts the user to do so
            this.navigationHelper.navigateTo(R.id.device_not_secured_fragment)

        } else if (this.navigationHelper.isDeepLinkIntent(this.intent)) {

            // If there was a deep link then follow it
            this.navigationHelper.navigateToDeepLink(this.intent)

        } else {

            // Otherwise move home
            this.navigationHelper.navigateTo(R.id.companies_fragment)
        }
    }

    /*
     * Handle the result from other activities, such as AppAuth or lock screen activities
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        val model = this.binding.model!!

        // Handle login responses and reset state
        if (requestCode == Constants.LOGIN_REDIRECT_REQUEST_CODE) {

            model.isTopMost = true
            if (data != null) {
                this.onFinishLogin(data)
            }
        }

        // Handle logout responses and reset state
        else if (requestCode == Constants.LOGOUT_REDIRECT_REQUEST_CODE) {

            model.isTopMost = true
            this.onFinishLogout()
        }

        // Handle returning from the lock screen
        else if (requestCode == Constants.SET_LOCK_SCREEN_REQUEST_CODE) {

            model.isTopMost = true
            model.isDeviceSecured = DeviceSecurity.isDeviceSecured(this)
            this.navigateStart()
        }
    }

    /*
     * Handle deep links while the app is running
     */
    override fun onNewIntent(receivedIntent: Intent?) {

        super.onNewIntent(receivedIntent)

        if (this.navigationHelper.isDeepLinkIntent(receivedIntent)) {
            this.navigationHelper.navigateToDeepLink(receivedIntent)
        }
    }

    /*
     * Called from the device not secured fragment to prompt the user to set a PIN or password
     */
    fun openLockScreenSettings() {

        val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
        this.startActivityForResult(intent, Constants.SET_LOCK_SCREEN_REQUEST_CODE)
        this.binding.model!!.isTopMost = false
    }

    /*
     * Update the load state and session buttons during and after view load
     */
    private fun onLoadStateChanged(loaded: Boolean) {

        // Update our state
        val model = this.binding.model!!
        model.isDataLoaded = loaded

        // Ask the header buttons fragment to update
        val buttonFragment = this.supportFragmentManager.findFragmentById(R.id.header_buttons_fragment) as HeaderButtonsFragment
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
    private fun onHome() {

        // If we are not initialised, then support retrying by reinitialising the app
        val model = this.binding.model!!
        if (!model.isInitialised) {
            this.initialiseApp()
            return
        }

        // Clear any existing errors
        val errorFragment = this.supportFragmentManager.findFragmentById(R.id.main_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // Move to the home view
        this.navigationHelper.navigateTo(R.id.companies_fragment)

        // If there is an error loading data from the API then force a reload
        if (model.authenticator!!.isLoggedIn() && !model.isDataLoaded) {
            this.onReloadData(false)
        }
    }

    /*
     * Start the login redirect
     */
    private fun startLogin() {

        val model = this.binding.model!!
        model.isTopMost = false

        // Run on the UI thread since we present UI elements
        CoroutineScope(Dispatchers.Main).launch {

            val that = this@MainActivity
            try {

                // Start the redirect
                model.authenticator!!.startLogin(that, Constants.LOGIN_REDIRECT_REQUEST_CODE)

            } catch (ex: Throwable) {

                // Report errors such as those looking up endpoints
                model.isTopMost = true
                that.handleError(ex)
            }
        }
    }

    /*
     * After the post login page executes, we receive the login response here
     */
    private fun onFinishLogin(loginResponseIntent: Intent) {

        val model = this.binding.model!!

        // Switch to a background thread to perform the code exchange
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivity
            try {
                // Handle completion after login success, which will exchange the authorization code for tokens
                model.authenticator!!.finishLogin(loginResponseIntent)

                // Reload data after logging in
                withContext(Dispatchers.Main) {
                    that.onReloadData(false)
                }

            } catch (ex: Throwable) {

                // Report errors on the main thread
                withContext(Dispatchers.Main) {
                    that.handleError(ex)
                }

            } finally {

                // Allow deep links again, once the activity is topmost
                model.isTopMost = true
            }
        }
    }

    /*
     * Remove tokens and navigate to login required
     */
    private fun onStartLogout() {

        val model = this.binding.model!!
        model.isTopMost = false

        // Run on the UI thread since we present UI elements
        CoroutineScope(Dispatchers.Main).launch {

            val that = this@MainActivity
            try {

                // Trigger the logout process, which will remove tokens and redirect to clear the OAuth session cookie
                model.authenticator!!.startLogout(that, Constants.LOGOUT_REDIRECT_REQUEST_CODE)

            } catch (ex: Throwable) {

                // On error, only output logout errors to the console rather than impacting the end user
                val uiError = ErrorHandler().fromException(ex)
                if (!uiError.errorCode.equals(ErrorCodes.redirectCancelled)) {
                    ErrorConsoleReporter.output(uiError, that)
                }

                // Move to the login required view and update UI state
                that.onFinishLogout()
            }
        }
    }

    /*
     * Free resources and update the UI when we receive the logout response
     */
    private fun onFinishLogout() {

        // Free logout resources
        val model = this.binding.model!!
        model.authenticator!!.finishLogout()

        // Update state
        this.onLoadStateChanged(false)
        model.isTopMost = true

        // Move to the login required page
        this.navigationHelper.navigateTo(R.id.login_required_fragment)

        // Send an event to fragments that should no longer be visible
        EventBus.getDefault().post(UnloadEvent())
    }

    /*
     * Publish an event to update all active views
     */
    private fun onReloadData(causeError: Boolean) {

        val model = this.binding.model!!
        model.viewManager.setViewCount(2)
        EventBus.getDefault().post(ReloadEvent(causeError))
    }

    /*
     * Update token storage to make the access token act like it is expired
     */
    private fun onExpireAccessToken() {
        val model = this.binding.model!!
        model.authenticator!!.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    private fun onExpireRefreshToken() {
        val model = this.binding.model!!
        model.authenticator!!.expireRefreshToken()
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    private fun handleError(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if (error.errorCode.equals(ErrorCodes.redirectCancelled)) {

            // If the user has closed the Chrome Custom Tab without logging in, move to the Login Required view
            this.navigationHelper.navigateTo(R.id.login_required_fragment)

        } else {

            // Otherwise there is a technical error and we display summary details
            val errorFragment = this.supportFragmentManager.findFragmentById(R.id.main_error_summary_fragment) as ErrorSummaryFragment
            errorFragment.reportError(
                this.getString(R.string.main_error_hyperlink),
                this.getString(R.string.main_error_dialogtitle),
                error)
        }
    }

    /*
     * Access the application in a typed manner
     */
    private fun app(): Application {
        return this.application as Application
    }

    /*
     * Deep linking is disabled unless our activity is top most
     */
    fun isTopMostActivity(): Boolean {
        val model = this.binding.model!!
        return model.isTopMost
    }

    /*
     * Clean up resources when destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        this.app().setMainActivity(null)
    }
}
