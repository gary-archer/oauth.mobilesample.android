package authguidance.mobilesample.activities

import android.support.v7.app.AppCompatActivity
import authguidance.mobilesample.Application;
import authguidance.mobilesample.plumbing.oauth.Authenticator;
import authguidance.mobilesample.plumbing.utilities.HttpClient

/*
 * A base activity for our displays
 */
abstract class BaseActivity : AppCompatActivity() {

    /*
     * A utility to reduce code in activities
     */
    fun getHttpClient(): HttpClient {
        var app = application as Application
        val authenticator = Authenticator()
        return HttpClient(app.configuration.app, authenticator)
    }
}