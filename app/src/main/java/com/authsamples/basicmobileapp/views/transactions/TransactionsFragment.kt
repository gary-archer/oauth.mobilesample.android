package com.authsamples.basicmobileapp.views.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.app.MainActivityViewModel
import com.authsamples.basicmobileapp.databinding.FragmentTransactionsBinding
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadMainViewEvent
import com.authsamples.basicmobileapp.views.utilities.ViewConstants
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
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
        val companyId = this.arguments?.getString(ViewConstants.ARG_COMPANY_ID, "") ?: ""

        // Create the view model
        val mainViewModel: MainActivityViewModel by activityViewModels()
        val factory = TransactionsViewModelFactory(
            mainViewModel.fetchClient,
            mainViewModel.viewModelCoordinator,
            companyId,
            this.requireActivity().application
        )
        this.binding.model = ViewModelProvider(this, factory).get(TransactionsViewModel::class.java)

        // Create the recycler view
        val list = this.binding.listTransactions
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = TransactionArrayAdapter(this.requireContext(), this.binding.model!!)

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
        this.loadData()
    }

    /*
     * Receive messages
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mainViewEvent: ReloadMainViewEvent) {
        this.loadData(ViewLoadOptions(true, mainViewEvent.causeError))
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
    private fun loadData(options: ViewLoadOptions? = null) {

        val onComplete = { isForbidden: Boolean ->

            if (isForbidden) {

                // Navigate back to the home view for expected errors such as trying to access unauthorized data
                findNavController().navigate(R.id.companies_fragment, Bundle())

            } else {

                // Otherwise ask the recycler view to reload
                (this.binding.listTransactions.adapter as TransactionArrayAdapter).reloadData()
            }
        }

        // Ask the model class to do the work
        this.binding.model!!.callApi(options, onComplete)
    }
}
