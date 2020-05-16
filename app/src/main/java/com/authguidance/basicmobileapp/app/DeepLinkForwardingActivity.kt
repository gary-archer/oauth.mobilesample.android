package com.authguidance.basicmobileapp.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/*
 * Use the technique from the below link to prevent the main activity being duplicated during deep linking
 * https://labs.comcast.com/deep-linking-through-a-multiple-activity-stack-on-android
 */
class DeepLinkForwardingActivity : Activity() {

    /*
     * Application startup deep links
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.handleIntent(this.intent)
    }

    /*
     * Deep links while the app is running
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.handleIntent(intent)
    }

    /*
     * Handle the deep link in a controlled manner
     */
    private fun handleIntent(receivedIntent: Intent) {

        // If there is a deep link and the main activity is not topmost, ignore it and end this activity
        if (!this.app().isMainActivityTopMost()) {
            finish()
            return
        }

        // Otherwise activate the main activity and clear this one
        receivedIntent.setClass(this, MainActivity::class.java)
        receivedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(receivedIntent)
    }

    /*
     * Access the application in a typed manner
     */
    fun app(): Application {
        return this.application as Application
    }
}
