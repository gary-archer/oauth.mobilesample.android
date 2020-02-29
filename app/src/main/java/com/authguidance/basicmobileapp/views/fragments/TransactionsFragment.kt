package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.databinding.FragmentTransactionsBinding
import com.authguidance.basicmobileapp.views.activities.MainActivity
import com.authguidance.basicmobileapp.views.adapters.TransactionArrayAdapter
import com.authguidance.basicmobileapp.api.entities.CompanyTransactions
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
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

    private lateinit var binding: FragmentTransactionsBinding
    private lateinit var mainActivity: MainActivity
    private var companyId: String = ""

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get data passed in
        this.companyId = this.arguments?.getString(Constants.ARG_COMPANY_ID, "") ?: ""

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
        this.binding.fragmentHeadingText.text = String.format(format, this.companyId)

        // Subscribe to the reload event and load data
        EventBus.getDefault().register(this);
        this.loadData(false)
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this);
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

        // Inform the view manager so that the UI can be updated during load
        this.mainActivity.viewManager.onMainViewLoading()

        // First clear any previous errors
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.transactionsErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@TransactionsFragment
            try {

                // Call the API and supply options
                val options = ApiRequestOptions(causeError)
                val result = that.mainActivity.apiClient.getCompanyTransactions(that.companyId, options)

                // Switch back to the UI thread for rendering
                withContext(Dispatchers.Main) {
                    that.mainActivity.viewManager.onMainViewLoaded()
                    that.binding.listTransactions.visibility = View.VISIBLE
                    renderData(result)
                }
            } catch (uiError: UIError) {

                // Handle invalid input
                if (uiError.statusCode == 404 && uiError.errorCode == "company_not_found") {

                    // A deep link could provide an id such as 3, which is unauthorized
                    withContext(Dispatchers.Main) {
                        that.mainActivity.navController.popBackStack()
                        that.mainActivity.onHome()
                    }
                } else if (uiError.statusCode == 400 && uiError.errorCode == "invalid_company_id") {

                    // A deep link could provide an invalid id value such as 'abc'
                    withContext(Dispatchers.Main) {
                        that.mainActivity.navController.popBackStack()
                        that.mainActivity.onHome()
                    }
                } else {

                    // Report other errors
                    withContext(Dispatchers.Main) {

                        // Report errors calling the API
                        that.binding.listTransactions.visibility = View.GONE
                        that.mainActivity.viewManager.onMainViewLoadFailed(uiError)

                        // Render error details
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
     * Render API response data on the UI thread
     */
    private fun renderData(data: CompanyTransactions) {

        // Show transactions data
        val list = this.binding.listTransactions
        list.layoutManager = LinearLayoutManager(this.mainActivity)
        list.adapter = TransactionArrayAdapter(mainActivity, data.transactions.toList())
    }
}
