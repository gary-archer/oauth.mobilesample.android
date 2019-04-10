package authguidance.mobilesample

import authguidance.mobilesample.configuration.Configuration
import authguidance.mobilesample.configuration.ConfigurationLoader
import authguidance.mobilesample.plumbing.utilities.MobileLogger
import android.os.Looper
import android.content.Intent
import authguidance.mobilesample.activities.ErrorActivity

/*
 * Our custom application class
 */
class Application : android.app.Application() {

    // The system exception handler
    private lateinit var systemUncaughtHandler: Thread.UncaughtExceptionHandler;

    // The configuration is loaded during application startup
    lateinit var configuration: Configuration private set

    /*
     * Application startup logic
     */
    override fun onCreate() {
        super.onCreate()

        // Set the exception handler for startup errors
        this.systemUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> this.handleStartupError(e) }

        // Load application configuration
        this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)

        // After startup work, configure to catch activity errors
        this.configureActivityErrorHandler()
    }

    /*
     * Use the technique from the below post to handle activity errors without restarting the app
     * https://github.com/Idolon-V/android-crash-catcher
     */
    private fun configureActivityErrorHandler() {

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
     * Handle startup exceptions by terminating the app
     */
    private fun handleStartupError(exception: Throwable) {

        val logger = MobileLogger()
        logger.debug("STARTUP EXCEPTION")

        // Write details to debug output
        val text = exception.message ?: exception.toString()
        logger.debug(text)
    }

    /*
     * Handle activity exceptions by terminating the app
     */
    private fun handleActivityError(exception: Throwable) {

        val logger = MobileLogger()
        logger.debug("ACTIVITY EXCEPTION")

        // Navigate to the error view
        val errorIntent = Intent(this, ErrorActivity::class.java)
        errorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        errorIntent.putExtra("EXCEPTION_DATA", exception)
        startActivity(errorIntent)
    }
}