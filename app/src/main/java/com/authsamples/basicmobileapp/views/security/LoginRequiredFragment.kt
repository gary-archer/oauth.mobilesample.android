package com.authsamples.basicmobileapp.views.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.app.MainActivityViewModel
import com.authsamples.basicmobileapp.databinding.FragmentLoginRequiredBinding
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent

/*
 * The fragment to indicate that a login is required
 */
class LoginRequiredFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentLoginRequiredBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout
        this.binding = FragmentLoginRequiredBinding.inflate(inflater, container, false)

        // Create our view model using data from the main view model
        val mainViewModel: MainActivityViewModel by activityViewModels()
        val factory = LoginRequiredViewModelFactory(mainViewModel.eventBus)
        this.binding.model = ViewModelProvider(this, factory).get(LoginRequiredViewModel::class.java)

        // Notify that the main view has changed
        this.binding.model!!.eventBus.post(NavigatedEvent(false))
        return binding.root
    }
}
