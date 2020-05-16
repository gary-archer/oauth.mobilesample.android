package com.authguidance.basicmobileapp.views.companies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.app.MainActivitySharedViewModel
import com.authguidance.basicmobileapp.databinding.FragmentCompaniesBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.utilities.Constants
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

        // Get details that the main activity supplies to child views
        val sharedViewModel: MainActivitySharedViewModel by activityViewModels()

        // Create our own view model
        this.binding.model = CompaniesViewModel(
            sharedViewModel.apiClientAccessor,
            sharedViewModel.viewManager)

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
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.companies_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()
        val options = ApiRequestOptions(causeError)

        val that = this@CompaniesFragment
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Call the API
                model.companies = apiClient.getCompanyList(options).toList()

                // Render results on the main thread
                withContext(Dispatchers.Main) {
                    model.viewManager.onViewLoaded()
                    that.renderData()
                }

            } catch (uiError: UIError) {

                withContext(Dispatchers.Main) {

                    // Process errors on the main thread
                    model.viewManager.onViewLoadFailed(uiError)
                    errorFragment.reportError(
                        that.getString(R.string.companies_error_hyperlink),
                        that.getString(R.string.companies_error_dialogtitle),
                        uiError)

                    // Clear any existing data
                    model.companies = ArrayList()
                    that.renderData()
                }
            }
        }
    }

    /*
     * Render API response data
     */
    private fun renderData() {

        // Get view model items from the raw data
        val model = this.binding.model!!
        val viewModelItems = model.companies.map { CompanyItemViewModel(it) }

        // When a company is clicked we will navigate to transactions for the clicked company id
        val onItemClick = { viewModelItem: CompanyItemViewModel ->

            val args = Bundle()
            args.putString(Constants.ARG_COMPANY_ID, viewModelItem.company.id.toString())
            findNavController().navigate(R.id.transactions_fragment, args)
        }

        // Bind the data
        val list = this.binding.listCompanies
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = CompanyArrayAdapter(this.requireContext(), viewModelItems.toList(), onItemClick)
    }
}
