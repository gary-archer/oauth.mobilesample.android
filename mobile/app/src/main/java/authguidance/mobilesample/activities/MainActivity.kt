package authguidance.mobilesample.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import authguidance.mobilesample.R
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

        // Customise the title
        this.title = "Company List"

        // Call the API to get data
        getData()
    }

    /*
     * Do the work of calling the API
     */
    private fun getData() {

        // Make the HTTP call on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val httpClient = super.getHttpClient()
            val result = httpClient.callApi("GET", null,"/companies")

            // Switch back to the UI thread for rendering
            CoroutineScope(Dispatchers.Main).launch {
                if(result != null) {
                    renderData(result);
                }
            }
        }
    }

    /*
     * Render API data on the UI thread
     */
    private fun renderData(data: String) {

        // Set the company items
        val items = mutableListOf<String>()
        items += data

        // Update UI controls
        val list = findViewById<ListView>(R.id.listCompanies);
        list.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
    }
}
