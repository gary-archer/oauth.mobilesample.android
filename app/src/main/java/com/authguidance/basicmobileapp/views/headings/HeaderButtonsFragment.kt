package com.authguidance.basicmobileapp.views.headings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.databinding.FragmentHeaderButtonsBinding
import com.authguidance.basicmobileapp.plumbing.events.DataStatusEvent
import com.authguidance.basicmobileapp.plumbing.events.NavigatedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentHeaderButtonsBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout
        this.binding = FragmentHeaderButtonsBinding.inflate(inflater, container, false)
        this.binding.fragment = this

        // Create the view model, which informs other views via events
        this.binding.model = HeaderButtonsViewModel()
        return this.binding.root
    }

    /*
     * Wire up the reload click event which uses special handling to support long press events
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up custom logic for long clicks
        val model = this.binding.model!!
        this.binding.btnReloadData.setCustomClickListener(this::onReload)

        // Subscribe for events
        EventBus.getDefault().register(this)
    }

    /*
     * Change visibility based on whether showing a main view
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NavigatedEvent) {
        event.used()
    }

    /*
     * During API calls we disable session buttons and then re-enable them afterwards
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: DataStatusEvent) {
        this.binding.model!!.updateDataStatus(event.loaded)
    }

    /*
     * Call back the parent view to move home
     */
    @Suppress("UnusedPrivateMember")
    fun onHome(view: View) {
        val mainActivity = this.activity as MainActivity
        mainActivity.onHome()
    }

    /*
     * Handle reload clicks by calling the parent view
     */
    fun onReload(causeError: Boolean) {
        val mainActivity = this.activity as MainActivity
        mainActivity.onReloadData(causeError)
    }

    /*
     * Call back the parent view to expire the access token
     */
    @Suppress("UnusedPrivateMember")
    fun onExpireAccessToken(view: View) {
        val mainActivity = this.activity as MainActivity
        mainActivity.onExpireAccessToken()
    }

    /*
     * Call back the parent view to expire the refresh token
     */
    @Suppress("UnusedPrivateMember")
    fun onExpireRefreshToken(view: View) {
        val mainActivity = this.activity as MainActivity
        mainActivity.onExpireRefreshToken()
    }

    /*
     * Call back the parent view to start a logout
     */
    @Suppress("UnusedPrivateMember")
    fun onLogout(view: View) {

        this.binding.model!!.updateDataStatus(false)
        val mainActivity = this.activity as MainActivity
        mainActivity.onStartLogout()
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }
}
