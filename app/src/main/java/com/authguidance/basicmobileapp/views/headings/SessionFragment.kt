package com.authguidance.basicmobileapp.views.headings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentSessionBinding
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.plumbing.events.InitialLoadEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
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

        // Create and add the model
        val activityState = (this.context as MainActivity).getChildViewModelState()
        this.binding.model = SessionViewModel(
            activityState.apiClientAccessor,
            activityState.shouldShowSessionIdAccessor,
            this.getString(R.string.api_session_id))

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
     * Handle initial load events by showing the session id
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: InitialLoadEvent) {
        this.binding.model?.updateData()
    }

    /*
     * Handle reload events by showing the session id
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadEvent) {
        this.binding.model?.updateData()
    }

    /*
     * Handle logout events by clearing the session id
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UnloadEvent) {
        this.binding.model?.clearData()
    }
}
