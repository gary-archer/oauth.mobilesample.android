package authguidance.mobilesample.logic.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
        val loginResult = intent.getIntExtra(EXTRA_LOGIN_RESULT, 0)
        when {
            loginResult == 0 -> {

                // Trigger a login redirect when we first come here
                Log.d("GJA", "Login activity invoked due to missing token")
                this.startLogin()
            }
            loginResult < 0 ->

                // Handle completion after Chrome Custom Tab cancellation
                Log.d("GJA", "Login activity entered after login failure")

                // TODO: Show trouble signing in controls

            else -> {

                // Handle completion after login completion, which could be success or failure
                Log.d("GJA", "Login activity entered after login completion")

                // Handle completion after login success
                super.getAuthenticator().finishAuthorization(intent)

                // TODO: Report failures
                // TODO: Return immediately to original view via a deep link and maintain state
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
