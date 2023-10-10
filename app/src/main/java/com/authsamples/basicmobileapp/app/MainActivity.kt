package com.authsamples.basicmobileapp.app

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.authsamples.basicmobileapp.databinding.FragmentDeviceNotSecuredBinding
import com.authsamples.basicmobileapp.databinding.FragmentLoginRequiredBinding
import com.authsamples.basicmobileapp.databinding.FragmentSessionBinding
import com.authsamples.basicmobileapp.plumbing.events.LoginRequiredEvent
import com.authsamples.basicmobileapp.views.companies.CompaniesView
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryView
import com.authsamples.basicmobileapp.views.headings.HeaderButtonsView
import com.authsamples.basicmobileapp.views.headings.TitleView
import com.authsamples.basicmobileapp.views.transactions.TransactionsView
import com.authsamples.basicmobileapp.views.utilities.DeviceSecurity
import com.authsamples.basicmobileapp.views.utilities.NavigationHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The application's main activity
 */
@Suppress("TooManyFunctions")
class MainActivity : ComponentActivity() {

    private lateinit var model: MainActivityViewModel
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
        this.model = model

        // Do the initial render
        this.render()

        // Initialize the view model and move to a loaded state, to cause a re-render
        this.model.initialize(this::onLoaded)
    }

    /*
     * Lay out the tree of views for rendering
     */
    private fun render() {

        val that = this@MainActivity
        setContent {
            Column {

                // The title area and user info
                TitleView(
                    userInfoViewModel = that.model.getUserInfoViewModel()
                )

                // The top row of buttons
                HeaderButtonsView(
                    eventBus = that.model.eventBus,
                    onHome = that::onHome,
                    onReload = that::onReloadData,
                    onExpireAccessToken = that::onExpireAccessToken,
                    onExpireRefreshToken = that::onExpireRefreshToken,
                    onLogout = that::onStartLogout
                )

                // Show application level errors when applicable
                if (model.error != null) {

                    ErrorSummaryView(
                        model.errorSummaryData(),
                        modifier = Modifier.fillMaxWidth().wrapContentSize()
                    )
                }

                // The session view
                AndroidViewBinding(FragmentSessionBinding::inflate)

                // Create navigation objects
                val navHostController = rememberNavController()
                that.navigationHelper = NavigationHelper(navHostController) { model.isDeviceSecured }
                that.navigationHelper.deepLinkBaseUrl = that.model.configuration.oauth.deepLinkBaseUrl

                // The main view is a navigation graph that is swapped out during navigation
                NavHost(navController = navHostController, startDestination = "blank") {

                    composable("blank") {
                    }

                    composable("device_not_secured") {
                        AndroidViewBinding(FragmentDeviceNotSecuredBinding::inflate)
                    }

                    composable("companies") {
                        CompaniesView(model = that.model.getCompaniesViewModel(), navigationHelper = navigationHelper)
                    }

                    composable(
                        "transactions/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) {

                        val id = it.arguments?.getString("id") ?: ""
                        TransactionsView(
                            companyId = id,
                            model = that.model.getTransactionsViewModel(),
                            navigationHelper = navigationHelper
                        )
                    }

                    composable("login_required") {
                        AndroidViewBinding(FragmentLoginRequiredBinding::inflate)
                    }
                }
            }
        }
    }

    /*
     * Once loaded, register for events and do the startup navigation
     */
    private fun onLoaded() {

        this.model.eventBus.register(this)
        this.navigateStart()
    }

    /*
     * Navigate to the initial fragment when the app starts
     */
    private fun navigateStart() {

        if (!this.model.isDeviceSecured) {

            // If the device is not secured we will move to a view that prompts the user to do so
            this.navigationHelper.navigateTo("device_not_secured")

        } else if (this.navigationHelper.isDeepLinkIntent(this.intent)) {

            // If there was a deep link then follow it
            this.navigationHelper.navigateToDeepLink(this.intent)

        } else {

            // Otherwise start at the default fragment in nav_graph.xml, which is the companies view
            this.navigationHelper.navigateTo("companies")
        }
    }

    /*
     * Called from the device not secured fragment to prompt the user to set a PIN or password
     */
    fun openLockScreenSettings() {

        val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
        this.lockScreenLauncher.launch(intent)
        this.model.isTopMost = false
    }

    /*
     * Handle the result from configuring the lock screen
     */
    private fun onLockScreenCompleted() {

        this.model.isTopMost = true
        this.model.isDeviceSecured = DeviceSecurity.isDeviceSecured(this)
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
            this.navigationHelper.navigateTo("login_required")
        }

        this.model.startLogin(this.loginLauncher::launch, onCancelled)
    }

    /*
     * Finish a login when we receive the response intent, then reload data
     */
    private fun onFinishLogin(responseIntent: Intent?) {

        val onSuccess = {

            if (this.navigationHelper.getActiveViewName() == "login_required") {

                // If the user logs in from the login required view, then navigate home
                this.navigationHelper.navigateTo("companies")

            } else {

                // Otherwise we are handling expiry so reload data in the current view
                this.model.reloadData(false)
            }
        }

        val onCancelled = {
            this.navigationHelper.navigateTo("login_required")
        }

        this.model.finishLogin(responseIntent, onSuccess, onCancelled)
    }

    /*
     * Remove tokens and redirect to remove the authorization server session cookie
     */
    private fun onStartLogout() {

        val onError = {
            this.navigationHelper.navigateTo("login_required")
        }

        this.model.startLogout(this.logoutLauncher::launch, onError)
    }

    /*
     * Perform post logout actions
     */
    private fun onFinishLogout() {
        this.model.finishLogout()
        this.navigationHelper.navigateTo("login_required")
    }

    /*
     * Move to the home view, which forces a reload if already in this view
     */
    private fun onHome() {

        // Reset the main view's own error if required
        this.model.updateError(null)

        // If there is a startup error then retry initializing
        if (!this.model.isLoaded) {
            this.model.initialize(this::onLoaded)
            return
        }

        // Inspect the current view
        if (this.navigationHelper.getActiveViewName() == "login_required") {

            // Start a new login when logged out
            this.onLoginRequired(LoginRequiredEvent())

        } else {

            // Navigate to the home view unless already there
            if (this.navigationHelper.getActiveViewName() != "companies") {
                this.navigationHelper.navigateTo("companies")
            }

            // Force a data reload if recovering from errors
            this.model.reloadDataOnError()
        }
    }

    /*
     * Publish an event to update all active views
     */
    private fun onReloadData(causeError: Boolean) {
        this.model.reloadData(causeError)
    }

    /*
     * Update token storage to make the access token act like it is expired
     */
    private fun onExpireAccessToken() {
        this.model.expireAccessToken()
    }

    /*
     * Update token storage to make the refresh token act like it is expired
     */
    private fun onExpireRefreshToken() {
        this.model.expireRefreshToken()
    }

    /*
     * Deep linking is disabled unless our activity is top most
     */
    fun isTopMost(): Boolean {
        return this.model.isTopMost
    }

    /*
     * Clean up resources when destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        this.model.eventBus.unregister(this)
        (this.application as Application).setMainActivity(null)
    }
}
