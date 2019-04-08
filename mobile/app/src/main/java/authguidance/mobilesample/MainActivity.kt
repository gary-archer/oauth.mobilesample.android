package authguidance.mobilesample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import authguidance.mobilesample.plumbing.oauth.Authenticator
import authguidance.mobilesample.plumbing.utilities.HttpClient

/*
 * Our main activity shows the list view
 */
class MainActivity : AppCompatActivity() {

    /*
     * Default initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /*
     * Call the API in the resume event
     */
    override fun onResume() {
        super.onResume()

        val authenticator = Authenticator()
        val httpClient = HttpClient(authenticator)
        httpClient.callApi("https://api.authguidance-examples.com/api/companies")
    }
}
