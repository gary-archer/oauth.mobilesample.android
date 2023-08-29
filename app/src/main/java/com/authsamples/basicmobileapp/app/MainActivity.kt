package com.authsamples.basicmobileapp.app

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.databinding.ActivityMainBinding
import com.authsamples.basicmobileapp.plumbing.events.LoginRequiredEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadUserInfoEvent
import com.authsamples.basicmobileapp.views.utilities.DeviceSecurity
import com.authsamples.basicmobileapp.views.utilities.NavigationHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * Our Single Activity App's activity
 */
@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity() {

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
     * Do Android specific initialization, and allow the app to crash if any of this fails
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        (this.application as Application).setMainActivity(this)

        // Create the main view model the first time the view is created
        val model: MainActivityViewModel by viewModels()

        // Inflate the view, which will initially render the blank fragment
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.binding.model = model

        // Initialise the navigation system
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navigationHelper = NavigationHelper(navHostFragment) { model.isDeviceSecured }
        this.navigationHelper.deepLinkBaseUrl = this.binding.model!!.configuration.oauth.deepLinkBaseUrl

        // Swap the main view based on the deep linking location or use the default
        this.navigateStart()

        // Start listening for events
        EventBus.getDefault().register(this)
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

        val onCancelled = {
            this.navigationHelper.navigateTo(R.id.login_required_fragment)
        }

        this.binding.model!!.startLogin(this.loginLauncher::launch, onCancelled)
    }

    /*
     * Finish a login when we receive the response intent, then reload data
     */
    private fun onFinishLogin(responseIntent: Intent?) {

        val onSuccess = {
            this.onReloadData(false)
        }

        val onCancelled = {
            this.navigationHelper.navigateTo(R.id.login_required_fragment)
        }

        this.binding.model!!.finishLogin(responseIntent, onSuccess, onCancelled)
    }

    /*
     * Remove tokens and redirect to remove the authorization server session cookie
     */
    fun onStartLogout() {

        val onError = {
            this.navigationHelper.navigateTo(R.id.login_required_fragment)
        }

        this.binding.model!!.startLogout(this.logoutLauncher::launch, onError)
    }

    /*
     * Perform post logout actions
     */
    private fun onFinishLogout() {
        this.binding.model!!.finishLogout()
        this.navigationHelper.navigateTo(R.id.login_required_fragment)
    }

    /*
     * Handle home navigation
     */
    fun onHome() {

        // Reset error state
        this.binding.model!!.updateError(null)

        // Move to the home view, which forces a reload if already in this view
        this.navigationHelper.navigateTo(R.id.companies_fragment)
    }

    /*
     * Publish an event to update all active views
     */
    fun onReloadData(causeError: Boolean) {

        this.binding.model!!.apiViewEvents.clearState()
        EventBus.getDefault().post(ReloadMainViewEvent(causeError))
        EventBus.getDefault().post(ReloadUserInfoEvent(causeError))
    }

    /*
     * Update token storage to make the access token act like it is expired
     */
    fun onExpireAccessToken() {
        this.binding.model!!.authenticator.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    fun onExpireRefreshToken() {
        this.binding.model!!.authenticator.expireRefreshToken()
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
        EventBus.getDefault().unregister(this)
        (this.application as Application).setMainActivity(null)
    }
}
