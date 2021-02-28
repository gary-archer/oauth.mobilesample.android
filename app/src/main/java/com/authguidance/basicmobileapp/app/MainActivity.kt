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
import com.authguidance.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadUserInfoViewEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.headings.HeaderButtonsFragment
import com.authguidance.basicmobileapp.views.utilities.Constants
import com.authguidance.basicmobileapp.views.utilities.DeviceSecurity
import com.authguidance.basicmobileapp.views.utilities.NavigationHelper
import org.greenrobot.eventbus.EventBus

/*
 * Our Single Activity App's activity
 */
@Suppress("TooManyFunctions")
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
        val model = MainActivityViewModel(this::onLoginRequired, this::onMainLoadStateChanged)

        // Populate the shared view model used by child fragments
        this.createSharedViewModel(model)

        // Inflate the view, which will trigger child fragments to run
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.binding.model = model

        // Initialise the navigation system
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            as NavHostFragment
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
        sharedViewModel.apiViewEvents = model.apiViewEvents

        // Properties passed to the header buttons fragment
        sharedViewModel.isMainViewLoadedAccessor = model::isMainViewLoaded
        sharedViewModel.onHome = this::onHome
        sharedViewModel.onReload = this::onReloadData
        sharedViewModel.onExpireAccessToken = model::onExpireAccessToken
        sharedViewModel.onExpireRefreshToken = model::onExpireRefreshToken
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
            this.binding.model?.initialise(this.applicationContext)
            this.navigationHelper.deepLinkBaseUrl = this.binding.model?.configuration!!.oauth.deepLinkBaseUrl

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
     * Navigate to the initial fragment
     */
    private fun navigateStart() {

        if (!this.binding.model!!.isDeviceSecured) {

            // If the device is not secured we will move to a view that prompts the user to do so
            this.navigationHelper.navigateTo(R.id.device_not_secured_fragment)

        } else if (this.navigationHelper.isDeepLinkIntent(this.intent)) {

            // If there was a deep link then follow it
            this.navigationHelper.navigateToDeepLink(this.intent)

        } else {

            // Otherwise start at the default fragment in nav_graph.xml, which is the companies view
        }
    }

    /*
     * Handle the result from other activities, such as AppAuth or lock screen activities
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // Handle login responses
        if (requestCode == Constants.LOGIN_REDIRECT_REQUEST_CODE) {
            this.onFinishLogin(data)
        }

        // Handle logout responses and reset state
        else if (requestCode == Constants.LOGOUT_REDIRECT_REQUEST_CODE) {
            this.onFinishLogout()
        }

        // Handle returning from the lock screen
        else if (requestCode == Constants.SET_LOCK_SCREEN_REQUEST_CODE) {

            this.binding.model?.isTopMost = true
            this.binding.model?.isDeviceSecured = DeviceSecurity.isDeviceSecured(this)
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
        this.binding.model?.isTopMost = false
    }

    /*
     * Update the load state and session buttons during and after view load
     */
    private fun onMainLoadStateChanged(loaded: Boolean) {

        // Update our state
        this.binding.model?.isMainViewLoaded = loaded

        // Ask the header buttons fragment to update
        val buttonFragment =
            this.supportFragmentManager.findFragmentById(R.id.header_buttons_fragment) as HeaderButtonsFragment
        buttonFragment.update()
    }

    /*
     * Start a login redirect when the API View Events helper informs us that a permanent 401 has occurred
     */
    private fun onLoginRequired() {
        this.binding.model?.startLogin(this, this::handleError)
    }

    /*
     * Finish a login when we receive the response intent
     */
    private fun onFinishLogin(responseIntent: Intent?) {

        // Reload data after login
        val onSuccess = {
            this.onReloadData(false)
        }

        this.binding.model?.finishLogin(responseIntent, onSuccess, this::handleError)
    }

    /*
     * Remove tokens and redirect to remove the authorization server session cookie
     */
    private fun onStartLogout() {

        val onError = { ex: Throwable ->

            // On error, only output logout errors to the console rather than impacting the end user
            val uiError = ErrorHandler().fromException(ex)
            if (!uiError.errorCode.equals(ErrorCodes.redirectCancelled)) {
                ErrorConsoleReporter.output(uiError, this)
            }

            // Move to the login required view
            this.onFinishLogout()
        }

        // Ask the model to do the work
        this.binding.model?.startLogout(this, onError)
    }

    /*
     * Perform post logout actions
     */
    private fun onFinishLogout() {

        // Update state and free resources
        this.binding.model?.finishLogout()
        this.onMainLoadStateChanged(false)

        // Move to the login required page
        this.navigationHelper.navigateTo(R.id.login_required_fragment)

        // Send an event to fragments that should no longer be visible
        EventBus.getDefault().post(UnloadEvent())
    }

    /*
     * Handle home navigation
     */
    private fun onHome() {

        // If there is a startup error then retry initialising the app
        val model = this.binding.model!!
        if (!model.isInitialised) {
            this.initialiseApp()
        }

        if (model.isInitialised) {

            // Clear any existing errors
            val errorFragment =
                this.supportFragmentManager.findFragmentById(R.id.main_error_summary_fragment) as ErrorSummaryFragment
            errorFragment.clearError()

            // Move to the home view, which forces a reload if already in this view
            this.navigationHelper.navigateTo(R.id.companies_fragment)
        }
    }

    /*
     * Publish an event to update all active views
     */
    private fun onReloadData(causeError: Boolean) {

        this.binding.model?.apiViewEvents?.clearState()
        EventBus.getDefault().post(ReloadMainViewEvent(causeError))
        EventBus.getDefault().post(ReloadUserInfoViewEvent(causeError))
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
            val errorFragment =
                this.supportFragmentManager.findFragmentById(R.id.main_error_summary_fragment) as ErrorSummaryFragment
            errorFragment.reportError(
                this.getString(R.string.main_error_hyperlink),
                this.getString(R.string.main_error_dialogtitle),
                error
            )
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
    fun isTopMost(): Boolean {
        return this.binding.model!!.isTopMost
    }

    /*
     * Clean up resources when destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        this.app().setMainActivity(null)
    }
}
