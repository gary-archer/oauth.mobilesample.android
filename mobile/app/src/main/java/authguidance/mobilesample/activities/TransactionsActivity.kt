package authguidance.mobilesample.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import authguidance.mobilesample.R
import authguidance.mobilesample.entities.CompanyTransactions
import authguidance.mobilesample.logic.TransactionArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * This shows a list of transactions for a company
 */
class TransactionsActivity : BaseActivity() {

    private var companyId: Int = 0

    /*
     * Activity startup
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Standard app setup
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        // Set the title
        this.companyId = this.intent.getIntExtra("COMPANY_ID", 0)
        this.title = "Transactions for Company ${this.companyId}"

        // Move back to the home activity when the home button is clicked
        val buttonHome = findViewById<Button>(R.id.btnHome)
        buttonHome.setOnClickListener {
            val intent = Intent(this, CompaniesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Get API data again when refresh is clicked
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
            val url = "companies/$companyId/transactions"
            val result = httpClient.callApi("GET", url, null, CompanyTransactions::class.java)

            // Switch back to the UI thread for rendering
            CoroutineScope(Dispatchers.Main).launch {
                renderData(result)
            }
        }
    }

    /*
     * Render API response data on the UI thread
     */
    private fun renderData(data: CompanyTransactions) {

        val list = findViewById<ListView>(R.id.listTransactions);
        list.adapter = TransactionArrayAdapter(this, data.transactions.toList())
    }
}
