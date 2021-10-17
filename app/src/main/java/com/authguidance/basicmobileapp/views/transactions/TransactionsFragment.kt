package com.authguidance.basicmobileapp.views.transactions

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
import com.authguidance.basicmobileapp.databinding.FragmentTransactionsBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import com.authguidance.basicmobileapp.views.utilities.Constants
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The fragment to show the transactions for a company
 */
class TransactionsFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentTransactionsBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the view
        this.binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        // Get data passed in
        val companyId = this.arguments?.getString(Constants.ARG_COMPANY_ID, "") ?: ""

        // Get details that the main activity supplies to child views
        val sharedViewModel: MainActivitySharedViewModel by activityViewModels()

        // Create our own view model
        this.binding.model = TransactionsViewModel(
            sharedViewModel.apiClientAccessor,
            sharedViewModel.apiViewEvents,
            companyId,
            this.getString(R.string.transactions_title)
        )

        return binding.root
    }

    /*
     * Wire up button click events to call back the activity
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
    fun onMessageEvent(mainViewEvent: ReloadMainViewEvent) {
        this.loadData(mainViewEvent.causeError)
    }

    /*
     * Load data for the fragment
     */
    private fun loadData(causeError: Boolean) {

        // Clear any errors from last time
        val errorFragment =
            this.childFragmentManager.findFragmentById(R.id.transactions_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // The success action renders the transactions returned
        val onSuccess = {
            this.renderData()
        }

        // The error action handles non success cases
        val onError = { uiError: UIError, isExpected: Boolean ->

            if (isExpected) {

                // Navigate back to the home view for expected errors such as trying to access unauthorized data
                val args = Bundle()
                findNavController().navigate(R.id.companies_fragment, args)

            } else {

                // Display technical errors
                errorFragment.reportError(
                    this.getString(R.string.transactions_error_hyperlink),
                    this.getString(R.string.transactions_error_dialogtitle),
                    uiError
                )

                // Update the display to clear any shown transactions
                this.renderData()
            }
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
        val viewModelItems = this.binding.model!!.transactions.map { TransactionItemViewModel(it) }

        // Render them via an adapter
        val list = this.binding.listTransactions
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = TransactionArrayAdapter(this.requireContext(), viewModelItems.toList())
    }
}
