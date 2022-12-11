package com.authsamples.basicmobileapp.views.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.app.MainActivityViewModel
import com.authsamples.basicmobileapp.databinding.FragmentUserInfoBinding
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadUserInfoEvent
import com.authsamples.basicmobileapp.plumbing.events.SetErrorEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The user info fragment renders logged in user details
 */
class UserInfoFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentUserInfoBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the view
        this.binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        // Create our view model using data from the main view model
        val mainViewModel: MainActivityViewModel by activityViewModels()
        val factory = UserInfoViewModelFactory(mainViewModel.apiClient, mainViewModel.apiViewEvents)
        this.binding.model = ViewModelProvider(this, factory).get(UserInfoViewModel::class.java)

        return binding.root
    }

    /*
     * Subscribe to events when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
     * Change visibility based on whether showing a main view
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NavigatedEvent) {

        if (event.isMainView) {

            // Load user info if required after navigating to a main view
            this.loadData()

        } else {

            // When not in a main view we are logged out, so clear user info
            this.binding.model!!.clearUserInfo()

            // Also ensure that any errors are cleared
            val clearEvent = SetErrorEvent(this.getString(R.string.userinfo_error_container), null)
            EventBus.getDefault().post(clearEvent)
        }
    }

    /*
     * Handle reload events
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadUserInfoEvent) {
        this.loadData(true, event.causeError)
    }

    /*
     * When logged in, call the API to get user info for display
     */
    private fun loadData(reload: Boolean = false, causeError: Boolean = false) {

        // Clear any errors from last time
        val clearEvent = SetErrorEvent(this.getString(R.string.userinfo_error_container), null)
        EventBus.getDefault().post(clearEvent)

        // Render errors on failure
        val onError = { uiError: UIError ->
            val setEvent = SetErrorEvent(this.getString(R.string.userinfo_error_container), uiError)
            EventBus.getDefault().post(setEvent)
        }

        // Ask the model class to do the work
        val options = UserInfoLoadOptions(
            reload,
            causeError
        )
        this.binding.model!!.callApi(options, onError)
    }
}
