package authguidance.mobilesample.logic.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.entities.Company
import authguidance.mobilesample.logic.adapters.CompanyArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * Our main activity shows the company list
 */
class CompaniesActivity : BaseActivity() {

    /*
     * Activity startup
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Standard app setup
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companies)

        // Reload the activity when home is clicked
        val buttonHome = findViewById<Button>(R.id.btnHome)
        buttonHome.setOnClickListener {
            this.recreate()
        }

        // Reload the activity when home is clicked
        val buttonRefresh = findViewById<Button>(R.id.btnRefreshData)
        buttonRefresh.setOnClickListener {
            this.getData()
        }

        // Load data on creation
        getData()
    }

    /*
     * Do the work of calling the API
     */
    private fun getData() {

        // Make the HTTP call on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val httpClient = super.getHttpClient()
            val result = httpClient.callApi("GET", "companies", null, Array<Company>::class.java)

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

        // Render the company data via the adapter class
        val list = findViewById<ListView>(R.id.listCompanies);
        list.adapter = CompanyArrayAdapter(this, companies.toList())

        // When an item is tapped, move to the transactions activity
        list.onItemClickListener = AdapterView.OnItemClickListener{ parent, _, position, _ ->

            // Get the company
            val company = parent.getItemAtPosition(position) as Company

            // Move to the transactions view
            val intent = Intent(this, TransactionsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("COMPANY_ID", company.id)
            startActivity(intent)
        }
    }
}
