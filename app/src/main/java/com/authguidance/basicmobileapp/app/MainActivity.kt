package com.authguidance.basicmobileapp.app

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.ActivityMainBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.ErrorConsoleReporter
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.events.DataStatusEvent
import com.authguidance.basicmobileapp.plumbing.events.InitializedEvent
import com.authguidance.basicmobileapp.plumbing.events.LoginRequiredEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadUserInfoViewEvent
import com.authguidance.basicmobileapp.plumbing.events.LoggedOutEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.utilities.DeviceSecurity
import com.authguidance.basicmobileapp.views.utilities.NavigationHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * Our Single Activity App's activity
 */
@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity(), MainActivityEvents {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationHelper: NavigationHelper

    // Handle launching the lock screen intent
    private val lockScreenLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        this.onLockScreenCompleted()
    }

    // Handle launching the login intent
    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        this.onFinishLogin(result.data!!)
    }

    // Handle launching the logout intent
    private val logoutLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        this.onFinishLogout()
    }

    /*
     * Do Android specific initialization and we allow the app to crash if any of this fails
     * The alternative leads to more complex code with lots of optionals
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        (this.application as Application).setMainActivity(this)

        // Create the view model the first time the view is created
        val model: MainActivityViewModel by viewModels()

        // Populate the shared view model provided to child fragments
        ViewModelProvider(
            this,
            MainActivitySharedViewModelFactory(model, this, this::isInLoginRequired)
        ).get(MainActivitySharedViewModel::class.java)

        // Inflate the view, which will trigger child fragments to run
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.binding.model = model

        // Initialise the navigation system
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            as NavHostFragment
        this.navigationHelper = NavigationHelper(navHostFragment) { model.isDeviceSecured }

        // Subscribe for events
        EventBus.getDefault().register(this)

        // Do initial navigation
        this.navigationHelper.deepLinkBaseUrl = this.binding.model!!.configuration.oauth.deepLinkBaseUrl
        this.navigateStart()
        EventBus.getDefault().post(InitializedEvent())
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
            this.navigationHelper.navigateTo(R.id.companies_fragment)
        }
    }

    /*
     * Called from the device not secured fragment to prompt the user to set a PIN or password
     */
    fun openLockScreenSettings() {

        val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
        this.lockScreenLauncher.launch(intent)
        this.binding.model!!.isTopMost = false
    }

    /*
     * Handle the result from configuring the lock screen
     */
    private fun onLockScreenCompleted() {

        this.binding.model!!.isTopMost = true
        this.binding.model!!.isDeviceSecured = DeviceSecurity.isDeviceSecured(this)
        this.navigateStart()
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
     * Start a login redirect when we are notified that we cannot call APIs
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginRequired(event: LoginRequiredEvent) {

        event.used()
        this.binding.model!!.startLogin(this.loginLauncher::launch, this::handleError)
    }

    /*
     * Finish a login when we receive the response intent
     */
    private fun onFinishLogin(responseIntent: Intent?) {

        // Reload data after login
        val onSuccess = {
            this.onReloadData(false)
        }

        this.binding.model!!.finishLogin(responseIntent, onSuccess, this::handleError)
    }

    /*
     * Remove tokens and redirect to remove the authorization server session cookie
     */
    override fun onStartLogout() {

        val onError = { ex: Throwable ->

            // On error, only output logout errors to the console rather than impacting the end user
            val uiError = ErrorHandler().fromException(ex)
            if (uiError.errorCode != ErrorCodes.redirectCancelled) {
                ErrorConsoleReporter.output(uiError, this)
            }

            // Move to the login required view
            this.onFinishLogout()
        }

        // Ask the model to do the work
        this.binding.model!!.startLogout(this.logoutLauncher::launch, onError)
    }

    /*
     * Perform post logout actions
     */
    private fun onFinishLogout() {

        // Update state and free resources
        this.binding.model!!.finishLogout()
        EventBus.getDefault().post(DataStatusEvent(false))

        // Move to the login required page
        this.navigationHelper.navigateTo(R.id.login_required_fragment)

        // Send an event to fragments that should no longer be visible
        EventBus.getDefault().post(LoggedOutEvent())
    }

    /*
     * Handle home navigation
     */
    override fun onHome() {

        // Clear any existing errors
        val errorFragment =
            this.supportFragmentManager.findFragmentById(R.id.main_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // Move to the home view, which forces a reload if already in this view
        this.navigationHelper.navigateTo(R.id.companies_fragment)
    }

    /*
     * Publish an event to update all active views
     */
    override fun onReloadData(causeError: Boolean) {

        this.binding.model!!.apiViewEvents.clearState()
        EventBus.getDefault().post(ReloadMainViewEvent(causeError))
        EventBus.getDefault().post(ReloadUserInfoViewEvent(causeError))
    }

    /*
     * A utility method to determine if we are in the login required view
     */
    fun isInLoginRequired(): Boolean {
        return this.navigationHelper.isInLoginRequired()
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    private fun handleError(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if (error.errorCode == ErrorCodes.redirectCancelled) {

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
        (this.application as Application).setMainActivity(null)
    }
}
