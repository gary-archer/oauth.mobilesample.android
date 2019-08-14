package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentTransactionsBinding
import authguidance.mobilesample.logic.activities.MainActivity
import authguidance.mobilesample.logic.adapters.TransactionArrayAdapter
import authguidance.mobilesample.logic.entities.CompanyTransactions
import authguidance.mobilesample.plumbing.utilities.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * The fragment to show the transactions for a company
 */
class TransactionsFragment : Fragment() {

    private lateinit var binding: FragmentTransactionsBinding
    private lateinit var mainActivity: MainActivity
    private var companyId: Int? = null

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get data passed in
        this.companyId = this.arguments?.getInt(Constants.ARG_COMPANY_ID, 0)

        // Inflate the view
        this.binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.getData()
    }

    /*
     * Do the work of calling the API
     */
    private fun getData() {

        val that = this
        CoroutineScope(Dispatchers.IO).launch {

            val httpClient = that.mainActivity.getHttpClient()
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

        val list = getView()?.findViewById<ListView>(R.id.listTransactions)
        list?.adapter = TransactionArrayAdapter(mainActivity, data.transactions.toList())
    }
}
