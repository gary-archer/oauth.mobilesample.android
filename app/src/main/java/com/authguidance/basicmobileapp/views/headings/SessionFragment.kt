package com.authguidance.basicmobileapp.views.headings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.app.MainActivityViewModel
import com.authguidance.basicmobileapp.databinding.FragmentSessionBinding
import com.authguidance.basicmobileapp.plumbing.events.LoggedOutEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadMainViewEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * Render the UI session id used in API logs
 */
class SessionFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentSessionBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the view
        this.binding = FragmentSessionBinding.inflate(inflater, container, false)

        // Create our view model using data from the main view model
        val mainViewModel: MainActivityViewModel by activityViewModels()
        this.binding.model = SessionViewModel(
            mainViewModel.apiClient,
            mainViewModel::shouldShowSessionId,
            this.getString(R.string.api_session_id)
        )

        return this.binding.root
    }

    /*
     * View initialization
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Subscribe to events
        EventBus.getDefault().register(this)
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    /*
     * Handle reload events by showing the session id
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mainViewEvent: ReloadMainViewEvent) {
        mainViewEvent.used()
        this.binding.model!!.updateData()
    }

    /*
     * Handle logout events by clearing the session id
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LoggedOutEvent) {
        event.used()
        this.binding.model!!.clearData()
    }
}
