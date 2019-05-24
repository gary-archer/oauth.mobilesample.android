package authguidance.mobilesample.logic.activities

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import authguidance.mobilesample.Application
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.fragments.HeaderButtonClickListener
import authguidance.mobilesample.plumbing.oauth.Authenticator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * This activity is invoked when a login is needed and immediately redirects
 * If login is cancelled or fails, this screen remains in place so that the user can retry
 */
class LoginActivity : BaseActivity(), HeaderButtonClickListener {

    private val RESULT_CODE_AUTHORIZE = 100

    /*
     * Standard initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Trigger the login process when we load
        this.startLogin()
    }

    /*
     * Override the base view to only show the home button
     */
    override fun showAllButtons(): Boolean {
        return false
    }

    /*
     * Start the login process and wait for the result
     */
    private fun startLogin() {

        CoroutineScope(Dispatchers.IO).launch {

            // Create the authenticator
            val app = application as Application
            val authenticator = Authenticator(app.configuration.oauth)

            // Ask it for a Chrome Custom Tabs authorization intent, so that we log in on a secure sandbox
            val intent = authenticator.getAuthorizationIntent(this@LoginActivity)

            // Start the intent
            ActivityCompat.startActivityForResult(this@LoginActivity, intent, RESULT_CODE_AUTHORIZE, null)
        }
    }
}
