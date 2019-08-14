package authguidance.mobilesample.logic.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import java.io.Serializable

/*
 * Our single activity application's only activity
 */
class MainActivity : AppCompatActivity() {

    // Navigation related fields
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment

    // The configuration is loaded once and a global authenticator object is created
    private lateinit var configuration: Configuration
    private lateinit var authenticator: Authenticator

    /*
     * Activity creation
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Set up data binding and navigation
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Give the application class a reference to ourself for exception handling purposes
        (this.application as Application).setActivity(this)

        // Run the application startup logic
        this.initialiseApp()
    }

    /*
     * Return an HTTP client to enable fragments to get data
     */
    fun getHttpClient(): HttpClient {
        return HttpClient(this.configuration.app, this.authenticator)
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    fun handleUnhandledException(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if(error.errorCode == "login_required") {

            // Start the AppAuth redirect to get a new token
            startLogin()
        }
        else {

            // Navigate to the error fragment to render it
            val args = Bundle()
            args.putSerializable("EXCEPTION_DATA", error as Serializable)
            navController.navigate(R.id.errorFragment, args)
        }
    }

    /*
     * Load the configuration and create the authenticator object, used to get tokens during API calls
     */
    private fun initialiseApp() {
        this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)
        this.authenticator = Authenticator(this.configuration.oauth, this)
    }

    /*
     * Initialise AppAuth processing
     */
    private fun startLogin() {
        println("GJA: AppAuth redirect")
    }
}
