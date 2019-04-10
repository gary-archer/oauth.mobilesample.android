package authguidance.mobilesample.activities

import android.os.Bundle
import authguidance.mobilesample.R
import authguidance.mobilesample.plumbing.oauth.Authenticator
import authguidance.mobilesample.plumbing.utilities.HttpClient

/*
 * Our main activity shows the list view
 */
class MainActivity : BaseActivity() {

    /*
     * Default initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Standard app setup
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

        // throw Exception("It all went horribly wrong");
    }
}
