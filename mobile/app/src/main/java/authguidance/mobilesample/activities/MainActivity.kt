package authguidance.mobilesample.activities

import android.os.Bundle
import authguidance.mobilesample.Application
import authguidance.mobilesample.R
import authguidance.mobilesample.plumbing.oauth.Authenticator
import authguidance.mobilesample.plumbing.utilities.HttpClient
import authguidance.mobilesample.plumbing.utilities.MobileLogger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

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

        // Handle button clicks
        this.btnHome.setOnClickListener{this.onHome()}
    }

    /*
     * Call the API in the resume event
     */
    override fun onResume() {
        super.onResume()

        // TODO: Reduce getting data to one liners - and turn off API security to make the request succeed
        // Get deserialization working, and error cases, and basic data displaying in list view

        var logger = MobileLogger();
        logger.debug("Starting async API call");

        // Get data for the view asynchronously
        GlobalScope.async {

            var app = application as Application;
            val authenticator = Authenticator()
            val httpClient = HttpClient(authenticator)

            logger.debug("Sending request")
            val value: Int = httpClient.callApi("${app.configuration.app.apiBaseUrl}/companies").await()
            logger.debug("Received response value $value")
        }

        // throw Exception("It all went horribly wrong");
    }

    /*
     * The home button will force a refresh later
     */
    private fun onHome() {
        val logger = MobileLogger()
        logger.debug("Home Button Clicked")
    }
}
