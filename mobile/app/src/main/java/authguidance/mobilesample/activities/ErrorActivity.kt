package authguidance.mobilesample.activities

import android.os.Bundle
import authguidance.mobilesample.R
import authguidance.mobilesample.plumbing.utilities.MobileLogger

/*
 * An activity to display unexpected error details
 */
class ErrorActivity : BaseActivity() {

    /*
     * Standard initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        MobileLogger.debug("Create error activity")
    }

    /*
     * Render error details in the on resume event
     */
    override fun onResume() {
        super.onResume()

        MobileLogger.debug("In error activity")
    }
}
