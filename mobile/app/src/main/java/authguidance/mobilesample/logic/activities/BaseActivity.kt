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
abstract class BaseActivity : FragmentActivity(), HeaderButtonClickListener {

    /*
     * By default activities use all buttons
     */
    override fun showAllButtons(): Boolean {

        // TODO: Only show the home button for the error activity
        return true
    }

    /*
     * The default home view action is to home to the companies view
     */
    override fun onHome() {
        val intent = Intent(this, CompaniesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /*
     * The default data refresh action is a no op
     */
    override fun onRefreshData() {
    }

    /*
     * Get the HTTP client before requesting data
     */
    protected fun getHttpClient(): HttpClient {
        var app = application as Application
        val authenticator = Authenticator(app.configuration.oauth)
        return HttpClient(app.configuration.app, authenticator)
    }
}