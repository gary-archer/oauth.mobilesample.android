package authguidance.mobilesample
import authguidance.mobilesample.plumbing.utilities.MobileLogger

/*
 * Our custom application class
 */
class Application : android.app.Application() {

    private lateinit var defaultExceptionHandler: Thread.UncaughtExceptionHandler

    override fun onCreate() {
        super.onCreate()

        // Override the default exception handler
        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e -> this.onException(t, e) }
    }

    /*
     * Unhandled exceptions are caught and handled here
     */
    private fun onException(thread: Thread, exception: Throwable) {

        // Do our handling
        val logger = MobileLogger()
        logger.debug("Unhandled exception")
        val text = exception.message ?: exception.toString()
        logger.debug(text)

        // Call the default handler and allow the app to terminate
        this.defaultExceptionHandler.uncaughtException(thread, exception)

        // This looks promising
        // https://github.com/Idolon-V/android-crash-catcher

        /*
        // HTTP errors
        // https://medium.com/yammer-engineering/handling-generic-http-errors-across-your-android-app-eaff4e49bbb2

        // Not working
        // https://stackoverflow.com/questions/30255905/is-it-impossible-to-start-a-activity-in-uncaughtexceptionhandler
        // https://stackoverflow.com/questions/6547072/how-do-i-start-another-activity-from-uncaughtexceptionhandler-uncaughtexception/10547176
        val intent = Intent(this, ErrorActivity::class.java)
        intent.putExtra("EXTRA_MY_EXCEPTION_HANDLER", BaseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // intent.putExtra("Exception", exception)
        this.startActivity(intent)
        */
    }
}