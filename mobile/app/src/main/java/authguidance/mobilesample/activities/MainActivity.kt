package authguidance.mobilesample.activities

import android.os.Bundle
import android.widget.TextView
import authguidance.mobilesample.R
import authguidance.mobilesample.plumbing.utilities.MobileLogger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        /*
         * TODO:
         * 1. Call wrong URL and present error - ErrorView constraint layout basics list of error fields
         * 2. Get real data and deserialize into objects via GSON
         * 3. Download images from REST API and display in Android?
         */

        CoroutineScope(Dispatchers.IO).launch {

            MobileLogger.debug("Calling API")

            // Make the HTTP call on a background thread
            val httpClient = super.getHttpClient()
            val result = httpClient.callApi("GET", "/companies")

            CoroutineScope(Dispatchers.Main).launch {

                // Update the UI on the main thread
                val debug = findViewById<TextView>(R.id.textDebug);
                debug.text = "Got result from API: $result";
            }
        }
    }

    /*
     * The home button will force a refresh later
     */
    private fun onHome() {
        MobileLogger.debug("Home Button Clicked")
    }
}
