package com.authguidance.basicmobileapp.views.fragments.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.databinding.FragmentUserInfoBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.plumbing.events.InitialLoadEvent
import com.authguidance.basicmobileapp.plumbing.events.UnloadEvent
import com.authguidance.basicmobileapp.views.fragments.ErrorSummaryFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        this.binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        // Create the model from the parent activity's data
        val mainActivity = this.context as MainActivity
        this.binding.model = UserInfoViewModel(mainActivity::getApiClient, mainActivity.viewManager)

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
        this.loadData(false)
    }

    /*
     * Handle reload events
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadEvent) {
        this.loadData(event.causeError)
    }

    /*
     * Handle logout events by clearing our data
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UnloadEvent) {
        this.clearData()
    }

    /*
     * When logged in, call the API to get user info for display
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
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.userInfoErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()
        val options = ApiRequestOptions(causeError)

        // Try to get data
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@UserInfoFragment
            try {
                // Call the API
                val userClaims = apiClient.getUserInfo(options)

                // Update the model and render results on the main thread
                withContext(Dispatchers.Main) {
                    model.viewManager.onViewLoaded()
                    model.setClaims(userClaims)
                }
            } catch (uiError: UIError) {

                // Process errors on the main thread
                model.setClaims(null)
                withContext(Dispatchers.Main) {
                    model.viewManager.onViewLoadFailed(uiError)
                    errorFragment.reportError(
                        that.getString(R.string.userinfo_error_hyperlink),
                        that.getString(R.string.userinfo_error_dialogtitle),
                        uiError)
                }
            }
        }
    }

    /*
     * Clear data after logging out
     */
    fun clearData() {

        // Clear the model
        this.binding.model!!.setClaims(null)

        // Also ensure that any errors are cleared
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.userInfoErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()
    }
}
