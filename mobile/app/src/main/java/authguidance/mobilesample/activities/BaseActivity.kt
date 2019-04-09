package authguidance.mobilesample.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import authguidance.mobilesample.plumbing.utilities.MobileLogger

/*
 * A base activity to handle exceptions
 */
abstract class BaseActivity : AppCompatActivity() {

    /*
     * Override and register an exception handler
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, e -> this.onException(e) }
    }

    /*
     * Unhandled exceptions are caught and we then transfer to the error activity
     */
    private fun onException(exception: Throwable) {

        val logger = MobileLogger()
        logger.debug("Unhandled exception")

        val text = exception.message ?: exception.toString()
        logger.debug(text)

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

        logger.debug("Moved to error activity")
    }
}