package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.api.entities.Company
import com.authguidance.basicmobileapp.databinding.FragmentCompaniesBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.views.ViewManager
import com.authguidance.basicmobileapp.views.adapters.CompanyArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The fragment to show the company list
 */
class CompaniesFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentCompaniesBinding

    // Details passed from the main activity
    private lateinit var apiClientAccessor: () -> ApiClient?
    private lateinit var viewManager: ViewManager

    /*
     * Get properties from the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        val mainActivity = context as MainActivity
        this.viewManager = mainActivity.viewManager
        this.apiClientAccessor = mainActivity::getApiClient
    }

    /*
     * Inflate the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentCompaniesBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    /*
     * View initialization
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.fragmentHeadingText.text = this.getString(R.string.company_list_title)

        // Subscribe to the reload event and load data
        EventBus.getDefault().register(this)
        this.loadData(false)
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    /*
     * Receive messages
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadEvent) {
        this.loadData(event.causeError)
    }

    /*
     * Load data for the fragment
     */
    private fun loadData(causeError: Boolean) {

        // Do not load if the app is not initialised yet
        val apiClient = this.apiClientAccessor()
        if (apiClient == null) {
            this.viewManager.onViewLoaded()
            return
        }

        // Inform the view manager so that a loading state can be rendered
        this.viewManager.onViewLoading()

        // First clear any previous content and errors
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.companiesErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()

        val that = this@CompaniesFragment
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Call the API and supply options
                val options = ApiRequestOptions(causeError)
                val result = apiClient.getCompanyList(options)

                // Switch back to the UI thread for rendering
                withContext(Dispatchers.Main) {
                    that.viewManager.onViewLoaded()
                    that.renderData(result)
                }
            } catch (uiError: UIError) {

                // Report errors
                withContext(Dispatchers.Main) {

                    // Report errors calling the API
                    that.viewManager.onViewLoadFailed(uiError)

                    // Render error details
                    errorFragment.reportError(
                        that.getString(R.string.companies_error_hyperlink),
                        that.getString(R.string.companies_error_dialogtitle),
                        uiError)
                }
            }
        }
    }

    /*
     * Render API response data on the UI thread
     */
    private fun renderData(companies: Array<Company>) {

        // Navigate to transactions for the clicked company id
        val onItemClick = { company: Company ->
            val args = Bundle()
            args.putString(Constants.ARG_COMPANY_ID, company.id.toString())
            findNavController().navigate(R.id.transactionsFragment, args)
        }

        // Bind the data
        val list = this.binding.listCompanies
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = CompanyArrayAdapter(this.context!!, companies.toList(), onItemClick)
    }
}
