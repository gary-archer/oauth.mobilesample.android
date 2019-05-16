package authguidance.mobilesample.activities

import android.os.Bundle
import android.util.Log
import android.widget.*
import authguidance.mobilesample.R
import authguidance.mobilesample.entities.Company
import authguidance.mobilesample.logic.CompanyArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
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

        // TODO: Set this in markup
        this.title = "Company List"

        // Button handlers
        val buttonHome = findViewById<Button>(R.id.btnHome);
        buttonHome.setOnClickListener {
            getData();
        }

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
        list.adapter = CompanyArrayAdapter(this, companies.toList())
        list.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->

            // Toast.makeText(this, "Clicked item : $position", Toast.LENGTH_SHORT).show()

            // TODO: Move activity
            val selectedItem = parent.getItemAtPosition(position) as Company
            Log.d("BasicMobileApp", "Selected item is ${selectedItem.id}")
        }
    }
}
