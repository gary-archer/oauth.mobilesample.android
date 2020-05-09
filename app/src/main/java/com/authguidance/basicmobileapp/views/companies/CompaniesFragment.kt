package com.authguidance.basicmobileapp.views.companies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.api.entities.Company
import com.authguidance.basicmobileapp.databinding.FragmentCompaniesBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
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

    private lateinit var binding: FragmentCompaniesBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the view
        this.binding = FragmentCompaniesBinding.inflate(inflater, container, false)

        // Create and add the model
        val mainActivity = this.context as MainActivity
        this.binding.model = CompaniesViewModel(mainActivity::getApiClient, mainActivity.viewManager)

        return this.binding.root
    }

    /*
     * View initialization
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Subscribe to the reload event and do the initial load of data
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

        // Get the model
        val model = this.binding.model!!

        // Do not try to load API data if the app is not initialised yet
        val apiClient = model.apiClientAccessor()
        if (apiClient == null) {
            model.viewManager.onViewLoaded()
            return
        }

        // Inform the view manager so that a loading state can be rendered
        model.viewManager.onViewLoading()

        // Initialise for this request
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.companiesErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()
        val options = ApiRequestOptions(causeError)

        val that = this@CompaniesFragment
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Call the API
                val result = apiClient.getCompanyList(options)

                // Render results on the main thread
                withContext(Dispatchers.Main) {
                    model.viewManager.onViewLoaded()
                    that.renderData(result)
                }
            } catch (uiError: UIError) {

                // Process errors on the main thread
                withContext(Dispatchers.Main) {
                    model.viewManager.onViewLoadFailed(uiError)
                    errorFragment.reportError(
                        that.getString(R.string.companies_error_hyperlink),
                        that.getString(R.string.companies_error_dialogtitle),
                        uiError)
                }
            }
        }
    }

    /*
     * Render API response data
     */
    private fun renderData(companies: Array<Company>) {

        // When a company is clicked we will navigate to transactions for the clicked company id
        val onItemClick = { company: Company ->
            val args = Bundle()
            args.putString(Constants.ARG_COMPANY_ID, company.id.toString())
            findNavController().navigate(R.id.transactionsFragment, args)
        }

        // Bind the data
        val list = this.binding.listCompanies
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter =
            CompanyArrayAdapter(
                this.requireContext(),
                companies.toList(),
                onItemClick
            )
    }
}
