package com.authguidance.basicmobileapp.app

import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.ActivityMainBinding
import com.authguidance.basicmobileapp.plumbing.utilities.NavigationHelper
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.events.InitialLoadEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
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

    // Model related properties
    private lateinit var binding: ActivityMainBinding
    private lateinit var childViewModelState: ChildViewModelState

    // Navigation properties
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    /*
     * Set up of the Single Activity App's main activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        this.app().setMainActivity(this)

        // Create our view model, and state needed for child fragments to create theirs
        val model = MainActivityViewModel(this::onLoadStateChanged, this::onLoginRequired)
        this.createChildViewModelState(model)

        // Inflate the view, which will trigger child fragments to run
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.binding.model = model

        // Initialise the navigation system
        this.navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navController = this.navHostFragment.navController

        // Finally, do the main application load
        this.initialiseApp()
    }

    /*
     * Create an object used to push main activity data to child fragments
     * The goal is a coding model similar to how child views bind to parent properties in React or SwiftUI
     */
    private fun createChildViewModelState(model: MainActivityViewModel) {

        val state = ChildViewModelState(model::apiClient, model.viewManager)

        // Properties passed to the header buttons fragment
        state.isDataLoadedAccessor = model::isDataLoaded
        state.onHome = this::onHome
        state.onReload = this::onReloadData
        state.onExpireAccessToken = this::onExpireAccessToken
        state.onExpireRefreshToken = this::onExpireRefreshToken
        state.onLogout = this::onStartLogout

        // Properties passed to the user info fragment
        state.shouldLoadUserInfoAccessor = {
                    model.isInitialised &&
                    model.isDeviceSecured &&
                    !this.isInLoginRequired()
        }

        // Properties passed to the session fragment
        state.shouldShowSessionIdAccessor = {
                    model.isInitialised &&
                    model.isDeviceSecured &&
                    model.authenticator!!.isLoggedIn()
        }

        this.childViewModelState = state
    }

    /*
     * Initialise the app and handle errors
     */
    private fun initialiseApp() {

        try {
            // Do the main view model initialisation
            this.binding.model!!.initialise(this.applicationContext)

            // Load the main navigation view
            this.navigateStart()

            // Send an initial load event to other views
            EventBus.getDefault().post(InitialLoadEvent())
        } catch (ex: Throwable) {

            // Display the startup error details
            this.handleException(ex)
        }
    }

    /*
     * Navigate to the start fragment, which is usually the companies view
     */
    private fun navigateStart() {

        val model = this.binding.model!!
        if (!model.isDeviceSecured) {

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
        val model = this.binding.model!!

        // Handle login responses and reset state
        if (requestCode == Constants.LOGIN_REDIRECT_REQUEST_CODE) {
            if (data != null) {
                model.isTopMost = true
                this.finishLogin(data)
            }
        }

        // Handle logout responses and reset state
        else if (requestCode == Constants.LOGOUT_REDIRECT_REQUEST_CODE) {
            model.isTopMost = true
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
        val model = this.binding.model!!
        model.isDataLoaded = loaded

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
    private fun onHome() {

        // If we are not initialised, then support retrying by reinitialising the app
        val model = this.binding.model!!
        if (!model.isInitialised) {
            this.initialiseApp()
            return
        }

        // Move to the home view
        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.companiesFragment
        )

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

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivity
            try {

                // Start the redirect
                model.authenticator!!.startLogin(that, Constants.LOGIN_REDIRECT_REQUEST_CODE)
            } catch (ex: Throwable) {

                // Report errors such as those looking up endpoints
                withContext(Dispatchers.Main) {
                    model.isTopMost = true
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
        val model = this.binding.model!!
        CoroutineScope(Dispatchers.IO).launch {

            // Switch to a background thread
            val that = this@MainActivity
            try {
                // Handle completion after login success, which will exchange the authorization code for tokens
                model.authenticator!!.finishLogin(loginResponseIntent)

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
                model.isTopMost = false
            }
        }
    }

    /*
     * Remove tokens and navigate to login required
     */
    private fun onStartLogout() {

        val model = this.binding.model!!
        try {

            // Trigger the logout process, which will remove tokens and redirect to clear the OAuth session cookie
            model.isTopMost = false
            model.authenticator!!.startLogout(this, Constants.LOGOUT_REDIRECT_REQUEST_CODE)
        } catch (ex: Throwable) {

            // Report errors such as those looking up endpoints
            model.isTopMost = true
            this.handleException(ex)
        }
    }

    /*
     * Free resources when we receive the logout response
     */
    private fun finishLogout() {

        // Finish processing
        val model = this.binding.model!!
        model.authenticator!!.finishLogout()

        // Update state
        this.onLoadStateChanged(false)
        model.isTopMost = false

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
    private fun onReloadData(causeError: Boolean) {

        val model = this.binding.model!!
        model.viewManager.setViewCount(2)
        EventBus.getDefault().post(ReloadEvent(causeError))
    }

    /*
     * Indicate whether in the login required view
     */
    private fun isInLoginRequired(): Boolean {

        val currentFragmentId =
            NavHostFragment.findNavController(this.navHostFragment).currentDestination?.id
        return currentFragmentId == R.id.loginRequiredFragment
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
    private fun handleException(exception: Throwable) {

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
    private fun app(): Application {
        return this.application as Application
    }

    /*
     * An object that can be called by child fragments
     */
    fun getChildViewModelState(): ChildViewModelState {
        return this.childViewModelState
    }

    /*
     * Deep linking is disabled unless our activity is top most
     */
    fun isTopMostActivity(): Boolean {
        val model = this.binding.model!!
        return model.isTopMost
    }

    /*
     * Clean up resources when destroyed, which occurs after the screen orientation is changed
     */
    override fun onDestroy() {
        super.onDestroy()
        this.app().setMainActivity(null)
    }
}
