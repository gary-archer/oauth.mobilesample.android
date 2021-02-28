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
import com.authguidance.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.utilities.Constants
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
            sharedViewModel.apiViewEvents
        )

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
    fun onMessageEvent(event: ReloadMainViewEvent) {
        this.loadData(event.causeError)
    }

    /*
     * Load data for the fragment
     */
    private fun loadData(causeError: Boolean) {

        // Clear any errors from last time
        val errorFragment =
            this.childFragmentManager.findFragmentById(R.id.companies_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // The success action renders the companies returned
        val onSuccess = {
            this.renderData()
        }

        // The error action renders the error and also zero companies
        val onError = { uiError: UIError ->

            errorFragment.reportError(
                this.getString(R.string.companies_error_hyperlink),
                this.getString(R.string.companies_error_dialogtitle),
                uiError
            )

            this.renderData()
        }

        // Ask the model class to do the work
        this.binding.model?.callApi(
            ApiRequestOptions(causeError),
            onSuccess,
            onError
        )
    }

    /*
     * Render API response data
     */
    private fun renderData() {

        // Get view model items from the raw data
        val viewModelItems = this.binding.model!!.companies.map { CompanyItemViewModel(it) }

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
