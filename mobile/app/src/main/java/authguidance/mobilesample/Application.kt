package authguidance.mobilesample

import android.app.Activity
import android.app.Application
import authguidance.mobilesample.configuration.Configuration
import authguidance.mobilesample.plumbing.utilities.ConfigurationLoader
import authguidance.mobilesample.plumbing.utilities.MobileLogger
import android.os.Looper
import android.content.Intent
import android.os.Bundle
import authguidance.mobilesample.logic.activities.ErrorActivity
import java.io.Serializable

/*
 * Our custom application class
 */
class Application : android.app.Application(), Application.ActivityLifecycleCallbacks {

    // The configuration is loaded during application startup
    lateinit var configuration: Configuration private set

    // The system exception handler
    private lateinit var systemUncaughtHandler: Thread.UncaughtExceptionHandler;

    // The current activity
    private var currentActivity: Activity? = null;

    /*
     * Application startup logic
     */
    override fun onCreate() {
        super.onCreate()

        // First set an unhandled exception handler
        this.systemUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> this.handleActivityError(e) }

        // Load application configuration
        this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)
        this.configureExceptionLoop()

        // Set up callbacks so that we can track the active activity
        this.registerActivityLifecycleCallbacks(this)
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
     * Handle activity exceptions by terminating the app
     */
    private fun handleActivityError(exception: Throwable) {

        MobileLogger.debug("ACTIVITY EXCEPTION: ${exception.message}")

        // Navigate to the error view
        val errorIntent = Intent(this, ErrorActivity::class.java)
        errorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        errorIntent.putExtra("EXCEPTION_DATA", exception as Serializable);
        startActivity(errorIntent)
    }

    /*
     * Lifecycle events enable us to keep track of the current activity
     * TODO: Is there more than one and what happens on back???
     * Be careful about preventing disposal
     */
    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        this.currentActivity = activity;
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        this.currentActivity = null;
    }
}