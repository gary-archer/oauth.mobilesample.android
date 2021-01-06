package com.authguidance.basicmobileapp.plumbing.oauth.logout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.views.utilities.Constants

/*
 * A class similar to AppAuth's RedirectUriReceiverActivity to receive the logout response
 */
class LogoutRedirectUriReceiverActivity : Activity() {

    public override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)

        // Create an intent to notify the main activity that logout is complete, and clear this activity
        val logoutResponseIntent = Intent(this, MainActivity::class.java)
        logoutResponseIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // Send the logout result to our single activity
        this.setResult(Constants.LOGOUT_REDIRECT_REQUEST_CODE, logoutResponseIntent)
        this.startActivity(logoutResponseIntent)
    }
}
