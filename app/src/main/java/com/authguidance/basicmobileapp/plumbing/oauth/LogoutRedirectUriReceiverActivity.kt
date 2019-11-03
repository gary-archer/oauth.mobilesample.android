package com.authguidance.basicmobileapp.plumbing.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.authguidance.basicmobileapp.logic.activities.MainActivity
import com.authguidance.basicmobileapp.logic.utilities.Constants

/*
 * A simple class similar to AppAuth's RedirectUriReceiverActivity, for handling responses after the logout redirect to remove the session cookie
 */
class LogoutRedirectUriReceiverActivity: Activity() {

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