package com.authguidance.basicmobileapp

import android.os.Looper
import com.authguidance.basicmobileapp.logic.activities.MainActivity

/*
 * Our custom application class
 */
class Application : android.app.Application() {

    // The application state
    lateinit var state: ApplicationState

    // The system exception handler
    private var systemUncaughtHandler: Thread.UncaughtExceptionHandler? = null

    // Store a reference to the main activity to use for unhandled exception handling
    private var mainActivity: MainActivity? = null

    /*
     * Inject exception handling during application startup
     */
    override fun onCreate() {
        super.onCreate()

        // Create application state
        this.state = ApplicationState(this.applicationContext)

        // Catch unhandled exception when running a debug build
        if (BuildConfig.DEBUG) {
            this.configureDebugExceptionHandling()
        }
    }

    /*
     * Store a reference to the main activity or null
     */
    fun setMainActivity(activity: MainActivity?) {
        this.mainActivity = activity
    }

    /*
     * For productive development, catch and report unhandled exceptions visually
     */
    private fun configureDebugExceptionHandling() {

        // First set up an unhandled exception handler
        this.systemUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()

        // Do custom exception handling
        Thread.setDefaultUncaughtExceptionHandler {t, e ->

            if(e is Exception) {
                // Handle normal exceptions ourselves during development
                this.handleUnhandledException(e)
            }
            else {
                // Let the system handler deal with low level exceptions and crash the app
                this.systemUncaughtHandler!!.uncaughtException(t, e)
            }
        }

        // Listen for unhandled exceptions while the application runs
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
            } catch (ex: Exception) {
                this.handleUnhandledException(ex)
            }
        }
    }

    /*
     * When there is a development error, call the single activity to render errors
     */
    private fun handleUnhandledException(ex: Exception) {
        this.mainActivity?.handleException(ex)
    }
}