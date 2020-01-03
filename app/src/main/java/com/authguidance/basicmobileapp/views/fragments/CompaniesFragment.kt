package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentCompaniesBinding
import com.authguidance.basicmobileapp.views.activities.MainActivity
import com.authguidance.basicmobileapp.views.adapters.CompanyArrayAdapter
import com.authguidance.basicmobileapp.api.entities.Company
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * The fragment to show the company list
 */
class CompaniesFragment : androidx.fragment.app.Fragment(), ReloadableFragment {

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
        return this.binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.mainActivity.setButtonState()
        this.mainActivity.setFragmentTitle(this.getString(R.string.company_list_title))
        this.loadData()
    }

    /*
     * Load data for the fragment
     */
    override fun loadData() {

        val that = this@CompaniesFragment
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val result = that.mainActivity.getApiClient().getCompanyList()

                // Switch back to the UI thread for rendering
                withContext(Dispatchers.Main) {
                    that.renderData(result)
                }
            }
            catch(ex: Exception) {

                // Report errors
                withContext(Dispatchers.Main) {
                    that.mainActivity.handleException(ex)
                }
            }
        }
    }

    /*
     * Render API response data on the UI thread
     */
    private fun renderData(companies: Array<Company>) {

        // Navigate to transactions for the clicked company id
        val onItemClick = {company: Company ->
            val args = Bundle()
            args.putString(Constants.ARG_COMPANY_ID, company.id.toString())
            findNavController().navigate(R.id.transactionsFragment, args)
        }

        // Bind the data
        val list = this.binding.listCompanies
        list.layoutManager = LinearLayoutManager(this.mainActivity)
        list.adapter = CompanyArrayAdapter(mainActivity, companies.toList(), onItemClick)
    }
}
