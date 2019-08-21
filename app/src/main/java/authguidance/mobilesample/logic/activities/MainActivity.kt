package authguidance.mobilesample.logic.activities

import android.app.PendingIntent
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import authguidance.mobilesample.Application
import authguidance.mobilesample.R
import authguidance.mobilesample.configuration.Configuration
import authguidance.mobilesample.databinding.ActivityMainBinding
import authguidance.mobilesample.plumbing.api.HttpClient
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import authguidance.mobilesample.plumbing.oauth.Authenticator
import authguidance.mobilesample.plumbing.utilities.ConfigurationLoader
import authguidance.mobilesample.plumbing.utilities.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Our single activity application's only activity
 */
class MainActivity : AppCompatActivity() {

    // Navigation related fields
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment

    // The configuration is loaded once, to create a global authenticator object
    private lateinit var configuration: Configuration
    private lateinit var authenticator: Authenticator

    // Login details
    private var loginContinuation: Continuation<Unit>? = null

    /*
     * Activity creation
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // TODO: Click button effect

        super.onCreate(savedInstanceState)

        // Set up data binding and navigation
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Give the application class a reference to the activity, for receiving unhandled exceptions
        (this.application as Application).setActivity(this)

        // Initialise the app
        this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)
        this.authenticator = Authenticator(this.configuration.oauth, this)
    }

    /*
     * Allow each main fragment to set the tirle
     */
    fun setFragmentTitle(title: String) {
        this.binding.fragmentTitle.text = title
    }

    /*
     * Return an HTTP client to enable fragments to get data
     */
    fun getHttpClient(): HttpClient {
        return HttpClient(this.configuration.app, this.authenticator)
    }

    /*
     * Start the login redirect
     */
    suspend fun startLogin() {

        return suspendCoroutine { continuation ->

            // Store the continuation
            this.loginContinuation = continuation

            // Start the asynchronous work
            CoroutineScope(Dispatchers.IO).launch {

                // The activity's this reference
                val that = this@MainActivity

                // Return here with a positive response if authorization succeeds
                val successIntent = Intent(that, MainActivity::class.java)
                successIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                successIntent.putExtra(Constants.ARG_LOGIN_RESULT, 1)

                // Return here with a negative response if authorization fails
                val failureIntent = Intent(that, MainActivity::class.java)
                failureIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                failureIntent.putExtra(Constants.ARG_LOGIN_RESULT, -1)

                // Start the redirect
                that.authenticator.startAuthorization(
                    PendingIntent.getActivity(that, 0, successIntent, 0),
                    PendingIntent.getActivity(that, 0, failureIntent, 0),
                    that.getTabColor())
            }
        }
    }

    /*
     * Receive the login response as a new intent for the existing single activity
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // TODO: Monitor intents that don't return here, which occurs after expire refresh token intermittently
        println("GJA: intent received")

        if(intent != null) {

            // See if we are processing a login response from the Chrome Custom Tab
            val loginResult = intent.getIntExtra(Constants.ARG_LOGIN_RESULT, 0)
            if(loginResult != 0) {

                // Handle completion after login completion, which could be a success or failure response
                CoroutineScope(Dispatchers.IO).launch {

                    println("GJA: Login result numeric is : $loginResult")
                    val that = this@MainActivity

                    try {
                        // Handle completion after login success, which will exchange the authorization code for tokens
                        that.authenticator.finishAuthorization(intent)

                        // Indicate success
                        that.loginContinuation?.resumeWith(Result.success(Unit))
                        that.loginContinuation = null
                    }
                    catch(exception: Exception) {

                        // Complete the continuation and return to Authenticator.getAccessToken
                        // The originating API call will then complete with the access token
                        that.loginContinuation?.resumeWithException(exception)
                        that.loginContinuation = null
                    }

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
    fun logout() {
        this.authenticator.logout()
        navController.navigate(R.id.loginRequiredFragment)
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    fun handleUnhandledException(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if(error.errorCode == "login_cancelled") {

            // If the user has closed the Chrome Custom Tab then move to Login Required
            navController.navigate(R.id.loginRequiredFragment)
        }
        else {

            // Otherwise navigate to the error fragment and render error details
            val args = Bundle()
            args.putSerializable(Constants.ARG_ERROR_DATA, error as Serializable)
            navController.navigate(R.id.errorFragment, args)
        }
    }

    /*
     * Use the App Bar's colour for the Chrome Custom Tab header
     */
    private fun getTabColor(): Int {
        return getColor(R.color.primary)
    }
}