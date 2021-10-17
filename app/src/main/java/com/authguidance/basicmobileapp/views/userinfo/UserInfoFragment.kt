package com.authguidance.basicmobileapp.views.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.app.MainActivitySharedViewModel
import com.authguidance.basicmobileapp.databinding.FragmentUserInfoBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.InitialLoadEvent
import com.authguidance.basicmobileapp.plumbing.events.ReloadUserInfoViewEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
import com.authguidance.basicmobileapp.views.errors.ErrorSummaryFragment
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
    ): View? {

        // Inflate the view
        this.binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        // Get details that the main activity supplies to child views
        val sharedViewModel: MainActivitySharedViewModel by activityViewModels()

        // Create our own view model
        this.binding.model = UserInfoViewModel(
            sharedViewModel.apiClientAccessor,
            sharedViewModel.apiViewEvents,
            sharedViewModel.shouldLoadUserInfoAccessor
        )

        return binding.root
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
     * Handle initial load events
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: InitialLoadEvent) {
        event.used()
        this.loadData(false)
    }

    /*
     * Handle reload events
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mainViewEvent: ReloadUserInfoViewEvent) {
        this.loadData(mainViewEvent.causeError)
    }

    /*
     * Handle logout events by clearing our data
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UnloadEvent) {
        event.used()
        this.clearData()
    }

    /*
     * When logged in, call the API to get user info for display
     */
    private fun loadData(causeError: Boolean) {

        // Clear any errors from last time
        val errorFragment =
            this.childFragmentManager.findFragmentById(R.id.userinfo_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // Render errors on failure
        val onError = { uiError: UIError ->

            errorFragment.reportError(
                this.getString(R.string.userinfo_error_hyperlink),
                this.getString(R.string.userinfo_error_dialogtitle),
                uiError
            )
        }

        // Ask the model class to do the work
        this.binding.model?.callApi(
            ApiRequestOptions(causeError),
            onError
        )
    }

    /*
     * Clear data after logging out
     */
    fun clearData() {

        // Clear the model
        this.binding.model?.setUserInfo(null)

        // Also ensure that any errors are cleared
        val errorFragment =
            this.childFragmentManager.findFragmentById(R.id.userinfo_error_summary_fragment) as ErrorSummaryFragment
        errorFragment.clearError()
    }
}
