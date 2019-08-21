package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class TransactionsFragment : androidx.fragment.app.Fragment(), ReloadableFragment {

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

        val format = this.getString(R.string.transactions_title)
        this.mainActivity.setFragmentTitle(String.format(format, this.companyId))
        this.getData()
    }

    /*
     * Reload data when the button is clicked
     */
    override fun reloadData() {
        this.getData()
    }

    /*
     * Do the work of calling the API
     */
    private fun getData() {

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@TransactionsFragment
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

        val list = this.binding.listTransactions
        list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.mainActivity)
        list.adapter = TransactionArrayAdapter(mainActivity, data.transactions.toList())
    }
}
