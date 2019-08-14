package authguidance.mobilesample

import android.os.Looper
import authguidance.mobilesample.logic.activities.MainActivity

/*
 * Our custom application class
 */
class Application : android.app.Application() {

    // The system exception handler
    private lateinit var systemUncaughtHandler: Thread.UncaughtExceptionHandler

    // The main activity
    private lateinit var activity: MainActivity;

    /*
     * Inject exception handling during application startup
     */
    override fun onCreate() {
        super.onCreate()

        println("GJA: Setting unhandled exception handler")

        // First set up an unhandled exception handler
        // this.systemUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e -> this.handleUnhandledException(t, e) }

        // Listen for unhandled exceptions while the application runs
        this.configureExceptionLoop()
    }

    /*
     * Store a reference to the main activity
     */
    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    /*
    * Use the technique from the below post to handle activity errors without restarting the app
    * https://github.com/Idolon-V/android-crash-catcher
    */
    private fun configureExceptionLoop() {

        while (true) {
            try {
                Looper.loop()
            } catch (e: Throwable) {
                println("GJA: exception loop")
                this.handleUnhandledException(Thread.currentThread(), e)
            }
        }
    }

    /*
     * Call the single activity to render errors
     */
    private fun handleUnhandledException(thread: Thread, exception: Throwable) {
        println("GJA: unhandled exception: on thread ${thread.id} : ${exception.message}")
        activity?.handleUnhandledException(exception)
    }
}