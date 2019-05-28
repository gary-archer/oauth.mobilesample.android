package authguidance.mobilesample

import authguidance.mobilesample.configuration.Configuration
import authguidance.mobilesample.plumbing.utilities.ConfigurationLoader
import android.os.Looper
import android.content.Intent
import authguidance.mobilesample.logic.activities.ErrorActivity
import authguidance.mobilesample.logic.activities.LoginActivity
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import authguidance.mobilesample.plumbing.oauth.Authenticator
import java.io.Serializable

/*
 * Our custom application class
 */
class Application : android.app.Application() {

    // The system exception handler
    private lateinit var systemUncaughtHandler: Thread.UncaughtExceptionHandler

    // The configuration is loaded during application startup
    lateinit var configuration: Configuration private set

    // The authenticator is created during application startup
    lateinit var authenticator: Authenticator private set

    /*
     * Application startup logic
     */
    override fun onCreate() {
        super.onCreate()

        // First set an unhandled exception handler
        this.systemUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> this.handleActivityError(e) }

        // Load configuration and create the authenticator
        this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)
        this.authenticator = Authenticator(this.configuration.oauth, this)

        // Listen for unhandled exceptions
        this.configureExceptionLoop()
    }

    /*
     * Use the technique from the below post to handle activity errors without restarting the app
     * https://github.com/Idolon-V/android-crash-catcher
     */
    private fun configureExceptionLoop() {

        while (true) {
            try {
                Looper.loop()
            }
            catch(e: Throwable) {
                this.handleActivityError(e)
            }
        }
    }

    /*
     * Handle exceptions by moving to the error activity
     */
    private fun handleActivityError(exception: Throwable) {

        // Get the error as an object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if(error.errorCode == "login_required") {

            // Navigate to the login view for this known error condition
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(loginIntent)
        }
        else {
            // Navigate to the error view for other errors
            val errorIntent = Intent(this, ErrorActivity::class.java)
            errorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            errorIntent.putExtra("EXCEPTION_DATA", error as Serializable)
            startActivity(errorIntent)
        }
    }
}