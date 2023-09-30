package com.authsamples.basicmobileapp.views.companies

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
import com.authsamples.basicmobileapp.databinding.FragmentCompaniesBinding
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.views.utilities.ViewConstants
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
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
    ): View {

        // Inflate the view
        this.binding = FragmentCompaniesBinding.inflate(inflater, container, false)

        // Create the view model
        val mainViewModel: MainActivityViewModel by activityViewModels()
        val factory = CompaniesViewModelFactory(
            mainViewModel.fetchClient,
            mainViewModel.eventBus,
            mainViewModel.viewModelCoordinator,
            mainViewModel.app
        )
        this.binding.model = ViewModelProvider(this.requireActivity(), factory)[CompaniesViewModel::class.java]

        // Create the recycler view
        val list = this.binding.listCompanies
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = CompanyArrayAdapter(this.requireContext(), this.binding.model!!, this::onItemClick)

        // Notify that the main view has changed
        println("GJA: navigated to companies")
        this.binding.model!!.eventBus.post(NavigatedEvent(true))
        return this.binding.root
    }

    /*
     * View initialization
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Subscribe to events and do the initial load of data
        this.binding.model!!.eventBus.register(this)
        this.loadData()
    }

    /*
     * When an item is clicked, navigate to its transactions
     */
    fun onItemClick(viewModelItem: CompanyItemViewModel) {
        val args = Bundle()
        args.putString(ViewConstants.ARG_COMPANY_ID, viewModelItem.company.id.toString())
        findNavController().navigate(R.id.transactions_fragment, args)
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        this.binding.model!!.eventBus.unregister(this)
    }

    /*
     * Receive messages
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadDataEvent) {
        this.loadData(ViewLoadOptions(true, event.causeError))
    }

    /*
     * Load data for the fragment
     */
    private fun loadData(options: ViewLoadOptions? = null) {

        // Reload the recycler view on completion
        val onComplete = {
            (this.binding.listCompanies.adapter as CompanyArrayAdapter).reloadData()
        }

        // Ask the model class to do the work
        this.binding.model!!.callApi(options, onComplete)
    }
}
