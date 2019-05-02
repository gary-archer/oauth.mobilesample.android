package authguidance.mobilesample.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import authguidance.mobilesample.R
import authguidance.mobilesample.entities.Company
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * Our main activity shows the company list
 */
class ListActivity : BaseActivity() {

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

        /* TODO NEXT
           1: Improve error activity with the same UI fields as for SPA
           2: Refresh button in base activity
           3: Render a list view with a hyperlink so that I can do navigation
        */

        // Make the HTTP call on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val httpClient = super.getHttpClient()
            val result = httpClient.callApi("GET", "/companies", null, Array<Company>::class.java)

            // Switch back to the UI thread for rendering
            CoroutineScope(Dispatchers.Main).launch {
                renderData(result)
            }
        }
    }

    /*
     * Render API response data on the UI thread
     */
    private fun renderData(companies: Array<Company>) {

        // Set the company items
        val items = mutableListOf<String>()
        companies.forEach {
            items += "Id: ${it.id}, Name: ${it.name}, TargetUsd: ${it.targetUsd}"
        }

        // Update UI controls
        val list = findViewById<ListView>(R.id.listCompanies);
        list.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
    }
}
