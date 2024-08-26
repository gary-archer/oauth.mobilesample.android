package com.authsamples.finalmobileapp.app

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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.authsamples.finalmobileapp.R
import com.authsamples.finalmobileapp.plumbing.events.LoginRequiredEvent
import com.authsamples.finalmobileapp.views.companies.CompaniesView
import com.authsamples.finalmobileapp.views.errors.ErrorSummaryView
import com.authsamples.finalmobileapp.views.errors.ErrorViewModel
import com.authsamples.finalmobileapp.views.headings.HeaderButtonsView
import com.authsamples.finalmobileapp.views.headings.SessionView
import com.authsamples.finalmobileapp.views.headings.TitleView
import com.authsamples.finalmobileapp.views.security.DeviceNotSecuredView
import com.authsamples.finalmobileapp.views.security.LoginRequiredView
import com.authsamples.finalmobileapp.views.transactions.TransactionsView
import com.authsamples.finalmobileapp.views.utilities.DeviceSecurity
import com.authsamples.finalmobileapp.views.utilities.MainView
import com.authsamples.finalmobileapp.views.utilities.NavigationHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The application's main activity
 */
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
        actionBar?.hide()

        // Create the main view model the first time the view is created
        val model: MainActivityViewModel by viewModels()
        this.model = model

        // Do the view creation
        this.createViews()

        // Initialize the view model and move to a loaded state, to cause a re-render
        this.model.initialize(this::onLoaded)
    }

    /*
     * Lay out the tree of views for rendering
     */
    @Suppress("LongMethod")
    private fun createViews() {

        val that = this@MainActivity
        setContent {
            ApplicationTheme {
                Column {

                    // The title area and user info
                    TitleView(that.model.getUserInfoViewModel())

                    // The top row of buttons
                    HeaderButtonsView(
                        that.model.eventBus,
                        that::onHome,
                        that::onReloadData,
                        that::onExpireAccessToken,
                        that::onExpireRefreshToken,
                        that::onStartLogout
                    )

                    // Show application level errors when applicable
                    if (model.error.value != null) {

                        ErrorSummaryView(
                            ErrorViewModel(
                                model.error.value!!,
                                stringResource(R.string.main_error_hyperlink),
                                stringResource(R.string.main_error_dialogtitle)
                            ),
                            Modifier
                                .fillMaxWidth()
                                .wrapContentSize()
                        )
                    }

                    // The session view
                    SessionView(that.model.eventBus, that.model.fetchClient.sessionId)

                    // Create navigation objects
                    val navHostController = rememberNavController()
                    that.navigationHelper =
                        NavigationHelper(navHostController) { model.isDeviceSecured }
                    that.navigationHelper.deepLinkBaseUrl =
                        that.model.configuration.oauth.deepLinkBaseUrl

                    // The main view is a navigation graph that is swapped out during navigation
                    NavHost(navHostController, MainView.Blank) {

                        composable(MainView.Blank) {
                        }

                        composable(MainView.DeviceNotSecured) {
                            DeviceNotSecuredView(that.model.eventBus, that::openLockScreenSettings)
                        }

                        composable(MainView.Companies) {
                            CompaniesView(that.model.getCompaniesViewModel(), navigationHelper)
                        }

                        composable(
                            "${MainView.Transactions}/{id}",
                            listOf(navArgument("id") { type = NavType.StringType })
                        ) {

                            val id = it.arguments?.getString("id") ?: ""
                            TransactionsView(
                                id,
                                that.model.getTransactionsViewModel(),
                                navigationHelper
                            )
                        }

                        composable(MainView.LoginRequired) {
                            LoginRequiredView(that.model.eventBus)
                        }
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
            this.navigationHelper.navigateToDeviceNotSecured()

        } else if (this.navigationHelper.isDeepLinkIntent(this.intent)) {

            // If there was a startup deep link then follow it
            this.navigationHelper.navigateToDeepLink(this.intent)

        } else {

            // Otherwise start at the default companies view
            this.navigationHelper.navigateToPath(MainView.Companies)
        }
    }

    /*
     * Called from the device not secured fragment to prompt the user to set a PIN or password
     */
    private fun openLockScreenSettings() {

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
    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)

        if (this.navigationHelper.isDeepLinkIntent(intent)) {
            this.navigationHelper.navigateToDeepLink(intent)
        }
    }

    /*
     * Move to the login required view when the user needs to be prompted to sign in
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginRequired(event: LoginRequiredEvent) {
        event.used()
        this.navigationHelper.navigateToLoginRequired()
    }

    /*
     * Start a login redirect when required
     */
    private fun onStartLogin() {
        this.model.startLogin(this.loginLauncher::launch)
    }

    /*
     * Finish a login when we receive the response intent, then reload data
     */
    private fun onFinishLogin(responseIntent: Intent?) {

        val onSuccess = {
            this.navigationHelper.navigateAfterLogin()
        }

        val onCancelled = {
            this.navigationHelper.navigateToLoginRequired()
        }

        this.model.finishLogin(responseIntent, onSuccess, onCancelled)
    }

    /*
     * Remove tokens and redirect to remove the authorization server session cookie
     */
    private fun onStartLogout() {

        val onError = {
            this.navigationHelper.navigateToLoggedOut()
        }

        this.model.startLogout(this.logoutLauncher::launch, onError)
    }

    /*
     * Perform post logout actions
     */
    private fun onFinishLogout() {
        this.model.finishLogout()
        this.navigationHelper.navigateToLoggedOut()
    }

    /*
     * The home button either initiates a login or navigates home
     */
    private fun onHome() {

        // Reset the main view's own error if required
        this.model.updateError(null)

        // If there is a startup error then retry initializing
        if (!this.model.isLoaded) {
            this.model.initialize(this::onLoaded)
            return
        }

        if (!this.model.authenticator.isLoggedIn()) {

            // Start a new login when required
            this.onStartLogin()

        } else {

            // Otherwise navigate to the home view unless we are already there
            if (this.navigationHelper.getActiveViewName() != MainView.Companies) {
                this.navigationHelper.navigateToPath(MainView.Companies)
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
