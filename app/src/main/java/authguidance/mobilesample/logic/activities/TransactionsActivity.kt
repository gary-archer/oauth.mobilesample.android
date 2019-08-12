package authguidance.mobilesample.logic.activities

import android.os.Bundle
import android.widget.*
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.entities.CompanyTransactions
import authguidance.mobilesample.logic.adapters.TransactionArrayAdapter
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
        this.setContentView(R.layout.activity_transactions)

        // Set the title
        this.companyId = this.intent.getIntExtra("COMPANY_ID", 0)
        this.title = "Transactions for Company ${this.companyId}"

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

        val list = this.findViewById<ListView>(R.id.listTransactions);
        list.adapter =
            TransactionArrayAdapter(this, data.transactions.toList())
    }
}
