package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentCompaniesBinding
import authguidance.mobilesample.logic.activities.MainActivity
import authguidance.mobilesample.logic.adapters.CompanyArrayAdapter
import authguidance.mobilesample.logic.entities.Company
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.RuntimeException

/*
 * The fragment to show the company list
 */
class CompaniesFragment : Fragment() {

    private lateinit var binding: FragmentCompaniesBinding
    private lateinit var mainActivity: MainActivity

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Inflate the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentCompaniesBinding.inflate(inflater, container, false)
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
     * Start an HTTP request to the API for data
     */
    private fun getData() {

        val that = this
        CoroutineScope(Dispatchers.IO).launch {

            val httpClient = that.mainActivity.getHttpClient()
            val result = httpClient.callApi("GET", "companies", null, Array<Company>::class.java)

            // Switch back to the UI thread for rendering
            CoroutineScope(Dispatchers.Main).launch {
                that.renderData(result)
            }
        }
    }

    /*
     * Render API response data on the UI thread
     */
    private fun renderData(companies: Array<Company>) {

        // Render the company data via the adapter class
        val list = getView()?.findViewById<ListView>(R.id.listCompanies)
        list?.adapter = CompanyArrayAdapter(mainActivity, companies.toList())

        // When an item is tapped, move to the transactions activity
        list?.onItemClickListener = AdapterView.OnItemClickListener{ parent, _, position, _ ->

            // Get the company
            val company = parent.getItemAtPosition(position) as Company

            // Navigate to transactions with the company id
            val args = Bundle()
            args.putInt("COMPANY_ID", company.id)
            findNavController().navigate(R.id.transactionsFragment, args)
        }
    }
}
