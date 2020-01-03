package com.authguidance.basicmobileapp.views.activities

import android.app.admin.DevicePolicyManager
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.authguidance.basicmobileapp.Application
import com.authguidance.basicmobileapp.ApplicationState
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.ActivityMainBinding
import com.authguidance.basicmobileapp.views.fragments.ActionBarFragment
import com.authguidance.basicmobileapp.views.fragments.HeaderButtonsFragment
import com.authguidance.basicmobileapp.views.fragments.ReloadableFragment
import com.authguidance.basicmobileapp.plumbing.utilities.NavigationHelper
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.plumbing.errors.ErrorHandler
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.plumbing.utilities.SecureDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

/*
 * Our single activity application's only activity
 */
class MainActivity : AppCompatActivity() {

    // Navigation related fields
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment

    // Application state
    private lateinit var state: ApplicationState;

    /*
     * Create the activity in a safe manner, to set up navigation and data binding
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Set up application state
        this.app().setMainActivity(this)
        this.state = this.app().state

        // Set up data binding
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Set up navigation, which will point to the blank fragment until the device is secured
        this.navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navController = this.navHostFragment.navController

        // Before running the app, force the device to be secured
        if(!SecureDevice.isSecured(this)) {

            // If not then force a lock screen to be set
            this.state.isMainActivityTopMost = false
            this.openLockScreenSettings()
        } else {

            // Otherwise start the app normally
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
            this.startActivityForResult(intent, Constants.SET_LOCK_SCREEN_REQUEST_CODE);
        }

        // When the user selects the Exit option, exit the Android App
        val declineCallback = {
            this.finishAndRemoveTask()
        }

        // Invoke the dialog to prompt the user
        SecureDevice.forceLockScreenUpdate(this, agreeCallback, declineCallback)
    }

    /*
     * Run the startup logic
     */
    private fun startApp() {

        try {
            // Load application state the first time the activity is created
            this.state.load()
        }
        catch(ex: Exception) {

            // Change the button state to indicate that we will exit the app
            val buttonFragment = this.supportFragmentManager.findFragmentById(R.id.buttonHeaderFragment) as HeaderButtonsFragment
            buttonFragment.setStartupErrorState()

            // Display the startup error and the user can only exit the app
            this.handleException(ex)
            return
        }

        // Ask the action bar fragment to load user info
        val actionBarFragment =
            this.supportFragmentManager.findFragmentById(R.id.actionBarFragment) as ActionBarFragment
        actionBarFragment.loadUserInfo()

        // Deep link to a fragment to start the app if required, or just start the home fragment
        if(NavigationHelper().isDeepLinkIntent(this.intent)) {
            NavigationHelper().navigateToDeepLink(
                this.intent,
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment)
        }
        else {
            this.onHome()
        }
    }

    /*
     * Handle the result from the lock screen system activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // See if this is a response from the lock screen
        if(requestCode == Constants.SET_LOCK_SCREEN_REQUEST_CODE) {
            if(!SecureDevice.isSecured(this)) {

                // Exit the app if still not secured
                this.finishAndRemoveTask()
            }
            else {

                // Start the app now that the device is secure
                this.state.isMainActivityTopMost = true
                this.startApp()
            }
        }

        // Handle login responses and reset state
        else if(requestCode == Constants.LOGIN_REDIRECT_REQUEST_CODE) {
            if(data != null) {
                this.state.isMainActivityTopMost = true
                this.finishLogin(data)
            }
        }

        // Handle logout responses and reset state
        else if(requestCode == Constants.LOGOUT_REDIRECT_REQUEST_CODE) {
            this.state.isMainActivityTopMost = true
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
     * After a startup error, clicking the first button exits the app
     */
    fun onAbortStartup() {
        this.finishAndRemoveTask()
    }

    /*
     * Navigate home when requested
     */
    fun onHome() {

        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.companiesFragment)
    }

    /*
     * Access the application in a typed manner
     */
    fun app(): Application {
        return this.application as Application
    }

    /*
     * Allow each main fragment to set the tirle
     */
    fun setFragmentTitle(title: String) {
        this.binding.fragmentHeadingText.text = title
    }

    /*
     * The active fragment calls this upon loading
     */
    fun setButtonState() {
        val buttonFragment = this.supportFragmentManager.findFragmentById(R.id.buttonHeaderFragment) as HeaderButtonsFragment
        val activeFragment = this.navHostFragment.childFragmentManager.primaryNavigationFragment
        buttonFragment.setButtonEnabledState(activeFragment is ReloadableFragment, this.state.authenticator.isLoggedIn())
    }

    /*
     * Return an HTTP client to enable fragments to get data
     */
    fun getApiClient(): ApiClient {
        return this.state.apiClient
    }

    /*
     * Start the login redirect
     */
    private fun startLogin() {

        if (this.state.isMainActivityTopMost) {

            // Prevent re-entrancy if 2 fragments get a 401 at once and both try to force a login
            this.state.isMainActivityTopMost = false
            CoroutineScope(Dispatchers.IO).launch {

                val that = this@MainActivity
                try {

                    // Start the redirect
                    that.state.authenticator.startLogin(that, Constants.LOGIN_REDIRECT_REQUEST_CODE)
                } catch (ex: Exception) {

                    // Report errors such as those looking up endpoints
                    withContext(Dispatchers.Main) {
                        that.state.isMainActivityTopMost = true
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
                that.state.authenticator.finishLogin(loginResponseIntent)

                withContext(Dispatchers.Main) {

                    // Load user info after logging in
                    val actionBarFragment = that.supportFragmentManager.findFragmentById(R.id.actionBarFragment) as ActionBarFragment
                    actionBarFragment.loadUserInfo()

                    // Also reload data for the active fragment
                    val activeFragment = that.navHostFragment.childFragmentManager.primaryNavigationFragment
                    if(activeFragment is ReloadableFragment) {
                        activeFragment.loadData()
                    }

                    // Update button state after login
                    that.setButtonState()
                }
            }
            catch(ex: Exception) {

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
        this.state.authenticator.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    fun expireRefreshToken() {
        this.state.authenticator.expireRefreshToken()
    }

    /*
     * Remove tokens and navigate to login required
     */
    fun startLogout() {

        try {

            // Trigger the logout process, which will remove tokens and redirect to clear the OAuth session cookie
            this.state.isMainActivityTopMost = false
            this.state.authenticator.startLogout(this, Constants.LOGOUT_REDIRECT_REQUEST_CODE)
        }
        catch(ex: Exception) {

            // Report errors such as those looking up endpoints
            this.state.isMainActivityTopMost = true
            this.handleException(ex)
        }
    }

    /*
     * Free resources when we receive the logout response
     */
    private fun finishLogout() {

        // Free resources
        this.state.authenticator.finishLogout()

        // Update the UI to clear user info after logging out
        val actionBarFragment = this.supportFragmentManager.findFragmentById(R.id.actionBarFragment) as ActionBarFragment
        actionBarFragment.clearUserInfo()

        // Move to the login required page
        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.loginRequiredFragment)
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    fun handleException(exception: Exception) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if (error.errorCode == "login_required") {

            // Start a login redirect
            this.startLogin()
        }
        else if (error.errorCode == "login_cancelled") {

            // If the user has closed the Chrome Custom Tab without logging in, move to the Login Required view
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.loginRequiredFragment)
        }
        else {

            // Otherwise navigate to the error fragment and render error details
            val args = Bundle()
            args.putSerializable(Constants.ARG_ERROR_DATA, error as Serializable)
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.errorFragment,
                args)
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
