package authguidance.mobilesample.logic.activities

import android.os.Bundle
import android.util.Log
import authguidance.mobilesample.Application
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.fragments.HeaderButtonClickListener
import authguidance.mobilesample.plumbing.oauth.Authenticator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * An activity shown when a login is needed
 */
class LoginActivity : BaseActivity(), HeaderButtonClickListener {

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

            val app = application as Application
            val authenticator = Authenticator(app.configuration.oauth)

            val result = authenticator.startLogin()
            Log.d("GJA", "RESULT IS $result")
        }
    }
}
