package com.authsamples.basicmobileapp.views.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.ApiRequestOptions
import com.authsamples.basicmobileapp.app.MainActivityViewModel
import com.authsamples.basicmobileapp.databinding.FragmentTransactionsBinding
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authsamples.basicmobileapp.plumbing.events.SetErrorEvent
import com.authsamples.basicmobileapp.views.utilities.Constants
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
    ): View {

        // Inflate the view
        this.binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        // Get data passed in
        val companyId = this.arguments?.getString(Constants.ARG_COMPANY_ID, "") ?: ""

        // Get details that the main activity supplies to child views
        val mainViewModel: MainActivityViewModel by activityViewModels()

        // Create our own view model
        this.binding.model = TransactionsViewModel(
            mainViewModel.apiClient,
            mainViewModel.apiViewEvents,
            companyId,
            this.getString(R.string.transactions_title)
        )

        // Notify that the main view has changed
        EventBus.getDefault().post(NavigatedEvent(true))
        return binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Subscribe to events and do the initial load of data
        EventBus.getDefault().register(this)
        this.loadData(false)
    }

    /*
     * Receive messages
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mainViewEvent: ReloadMainViewEvent) {
        this.loadData(mainViewEvent.causeError)
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    /*
     * Load data for the fragment
     */
    private fun loadData(causeError: Boolean) {

        // Clear any errors from last time
        val clearEvent = SetErrorEvent(this.getString(R.string.transactions_error_container), null)
        EventBus.getDefault().post(clearEvent)

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
                val setEvent = SetErrorEvent(this.getString(R.string.transactions_error_container), uiError)
                EventBus.getDefault().post(setEvent)

                // Update the display to clear data
                this.renderData()
            }
        }

        // Ask the model class to do the work
        this.binding.model!!.callApi(
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
