package authguidance.mobilesample.logic.activities

import android.content.Intent
import android.support.v4.app.FragmentActivity
import authguidance.mobilesample.Application;
import authguidance.mobilesample.logic.fragments.HeaderButtonClickListener
import authguidance.mobilesample.plumbing.oauth.Authenticator;
import authguidance.mobilesample.plumbing.api.HttpClient

/*
 * A base activity for our displays
 */
abstract class BaseActivity : FragmentActivity() {

    /*protected fun onHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }*/

    protected fun getAuthenticator(): Authenticator {
        var app = application as Application
        return app.authenticator
    }

    protected fun getHttpClient(): HttpClient {
        var app = application as Application
        return HttpClient(app.configuration.app, this.getAuthenticator())
    }
}