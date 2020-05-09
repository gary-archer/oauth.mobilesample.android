package com.authguidance.basicmobileapp.views.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.databinding.FragmentTransactionsBinding
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.api.entities.CompanyTransactions
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus

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
        val companyId = this.arguments?.getString(Constants.ARG_COMPANY_ID, "") ?: "0"

        // Create and add the model
        val activityState = (this.context as MainActivity).getChildViewModelState()
        this.binding.model = TransactionsViewModel(
            activityState.apiClientAccessor,
            activityState.viewManager,
            companyId,
            this.getString(R.string.transactions_title))

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
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.transactions_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()
        val options = ApiRequestOptions(causeError)

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@TransactionsFragment
            try {

                // Call the API
                val result = apiClient.getCompanyTransactions(model.companyId, options)

                // Switch back to the UI thread for rendering
                withContext(Dispatchers.Main) {
                    model.viewManager.onViewLoaded()
                    that.renderData(result)
                }
            } catch (uiError: UIError) {

                // Process errors on the main thread
                val isExpected = that.handleApiError(uiError)
                if (isExpected) {

                    // Handle expected errors by navigating back to the home view
                    withContext(Dispatchers.Main) {
                        model.viewManager.onViewLoaded()
                        val args = Bundle()
                        findNavController().navigate(R.id.companies_fragment, args)
                    }
                } else {

                    // Report other errors on the main thread
                    withContext(Dispatchers.Main) {
                        model.viewManager.onViewLoadFailed(uiError)
                        errorFragment.reportError(
                            that.getString(R.string.transactions_error_hyperlink),
                            that.getString(R.string.transactions_error_dialogtitle),
                            uiError)
                    }
                }
            }
        }
    }

    /*
     * Handle 'business errors' received from the API
     */
    private fun handleApiError(error: UIError): Boolean {

        var isExpected = false

        if (error.statusCode == 404 && error.errorCode.equals(ErrorCodes.companyNotFound)) {

            // A deep link could provide an id such as 3, which is unauthorized
            isExpected = true
        } else if (error.statusCode == 400 && error.errorCode.equals(ErrorCodes.invalidCompanyId)) {

            // A deep link could provide an invalid id value such as 'abc'
            isExpected = true
        }

        return isExpected
    }

    /*
     * Render API response data
     */
    private fun renderData(data: CompanyTransactions) {

        val list = this.binding.listTransactions
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter =
            TransactionArrayAdapter(
                this.requireContext(),
                data.transactions.toList()
            )
    }
}
