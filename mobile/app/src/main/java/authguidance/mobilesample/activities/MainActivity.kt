package authguidance.mobilesample.activities

import android.os.Bundle
import authguidance.mobilesample.Application
import authguidance.mobilesample.R
import authguidance.mobilesample.plumbing.oauth.Authenticator
import authguidance.mobilesample.plumbing.utilities.HttpClient
import authguidance.mobilesample.plumbing.utilities.MobileLogger
import com.github.kittinunf.fuel.core.Method
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking

/*
 * Our main activity shows the company list
 */
class MainActivity : BaseActivity() {

    /*
     * Activity startup
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Standard app setup
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle button clicks
        this.btnHome.setOnClickListener{this.onHome()}

        // Call the API to get data
        getData()
    }

    /*
     * Do the work of calling the API
     */
    private fun getData() {

        // TODO:
        // Focus on getting things working plus the coding model
        // 1. println string output
        // 2. textview string output
        // 3. textview error output
        // 4. textview object output
        // 5. presentation

        runBlocking {

            val logger = MobileLogger()
            logger.debug("Entered async region")

            // Get the HTTP client
            var app = application as Application
            val authenticator = Authenticator()
            val httpClient = HttpClient(authenticator)

            // Call it to get data
            logger.debug("Calling API")
            val result = httpClient.callApi("GET", "${app.configuration.app.apiBaseUrl}/companies").await()
            logger.debug("Got result from API")
            println(result)
        }
    }

    /*
     * The home button will force a refresh later
     */
    private fun onHome() {
        val logger = MobileLogger()
        logger.debug("Home Button Clicked")
    }

    /*
     * Call the API in the resume event
     */
    override fun onResume() {

        /*
        super.onResume()

        // TODO: Reduce getting data to one liners - and turn off API security to make the request succeed
        // Get deserialization working, and error cases, and basic data displaying in list view

        var logger = MobileLogger();
        logger.debug("Starting async API call");

        var app = application as Application;
        val authenticator = Authenticator()
        val httpClient = HttpClient(authenticator)

        logger.debug("Sending request")
        val result = async {

        }

        val value: Int = httpClient.callApi("${app.configuration.app.apiBaseUrl}/companies").await()
        logger.debug("Received response value $value")

        // Get data for the view asynchronously
        try {

        } catch(ex: Throwable) {
        }

        var response
        GlobalScope.async {

            logger.debug("Sending request")
            val value: Int = httpClient.callApi("${app.configuration.app.apiBaseUrl}/companies").await()
            logger.debug("Received response value $value")
        }

        // throw Exception("It all went horribly wrong");
        */
    }


}
