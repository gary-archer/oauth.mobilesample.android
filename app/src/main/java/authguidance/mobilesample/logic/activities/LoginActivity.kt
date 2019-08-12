package authguidance.mobilesample.logic.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.fragments.HeaderButtonClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * This activity is invoked when a login is needed and immediately redirects
 * If login is cancelled or fails, this screen remains in place so that the user can retry
 */
class LoginActivity : BaseActivity(), HeaderButtonClickListener {

    // Constants
    private val EXTRA_LOGIN_RESULT = "login_result"

    /*
     * Standard initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // TODO: Update application to indicate chrome custom tab is no longer active

        // Look at intent state
        val loginResult = this.intent.getIntExtra(EXTRA_LOGIN_RESULT, 0)
        when {
            loginResult == 0 -> {

                // Trigger a login redirect when we first come here
                this.startLogin()
            }
            loginResult < 0 -> {

                // Handle completion after Chrome Custom Tab cancellation
                // TODO: Update UI to indicate trouble signing in
                // TODO: Handle Chrome not installed errors
            }
            else -> {

                // Handle completion after login completion, which could be success or failure
                this.handleLoginResponse()
            }
        }
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

            // The activity's this reference
            val that = this@LoginActivity

            // Return here with a positive response if authorization succeeds
            val successIntent = Intent(that, LoginActivity::class.java)
            successIntent.putExtra(EXTRA_LOGIN_RESULT, 1)
            successIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Return here with a negative response if authorization fails
            val failureIntent = Intent(that, LoginActivity::class.java)
            failureIntent.putExtra(EXTRA_LOGIN_RESULT, -1)
            failureIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Start the redirect
            super.getAuthenticator().startAuthorization(
                that,
                that.getTabColor(),
                PendingIntent.getActivity(that, 0, successIntent, 0),
                PendingIntent.getActivity(that, 0, failureIntent, 0))

            // TODO: Update application to indicate chrome custom tab is active
        }
    }

    /*
     * Handle a login response from the Authorization Server, which could be a success or failure response
     */
    private fun handleLoginResponse() {

        CoroutineScope(Dispatchers.IO).launch {

            // The activity's this reference
            val that = this@LoginActivity

            // Handle completion after login success
            val success = super.getAuthenticator().finishAuthorization(that)
            if (success) {

                // For now we always return to the home view on success
                val intent = Intent(that, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

    }

    /*
     * Use the App Bar's colour for the Chrome Custom Tab header
     */
    private fun getTabColor(): Int {

        val color =  R.color.colorPrimary
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getColor(color)
        } else {
            resources.getColor(color)
        }
    }
}
