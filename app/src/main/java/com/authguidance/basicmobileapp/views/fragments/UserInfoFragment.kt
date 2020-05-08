package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.databinding.FragmentUserInfoBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.events.ReloadEvent
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.views.ViewManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The user info fragment shows the logged in user
 */
class UserInfoFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentUserInfoBinding

    // Details passed from the main activity
    private lateinit var apiClientAccessor: () -> ApiClient?
    private lateinit var viewManager: ViewManager

    /*
     * Get properties from the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        val mainActivity = context as MainActivity
        this.viewManager = mainActivity.viewManager
        this.apiClientAccessor = mainActivity::getApiClient
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * View initialization
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Subscribe to the reload event
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
     * Receive messages
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadEvent) {
        this.loadData(event.causeError)
    }

    /*
     * Handle external calls to load data
     */
    fun loadUserInfo() {
        this.loadData(false)
    }

    /*
     * Clear data after logging out
     */
    fun clearUserInfo() {

        // Blank out the user name
        this.binding.loggedInUser.text = ""

        // Also ensure that any errors are cleared
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.userInfoErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()
    }

    /*
     * When logged in, call the API to get user info for display
     */
    private fun loadData(causeError: Boolean) {

        val apiClient = this.apiClientAccessor()
        if (apiClient == null) {
            this.viewManager.onViewLoaded()
            return
        }

        // Inform the view manager so that a loading state can be rendered
        this.viewManager.onViewLoading()

        // Clear any existing errors
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.userInfoErrorSummaryFragment) as ErrorSummaryFragment
        errorFragment.clearError()

        // Try to get data
        CoroutineScope(Dispatchers.IO).launch {

            val that = this@UserInfoFragment
            try {
                // Clear any previous content
                that.binding.loggedInUser.text = ""

                // Load user info
                val options = ApiRequestOptions(causeError)
                val userInfo = apiClient.getUserInfo(options)

                // Render user info
                withContext(Dispatchers.Main) {
                    that.viewManager.onViewLoaded()
                    that.binding.loggedInUser.text = "${userInfo.givenName} ${userInfo.familyName}"
                }
            } catch (uiError: UIError) {

                withContext(Dispatchers.Main) {

                    // Report errors calling the API
                    that.viewManager.onViewLoadFailed(uiError)

                    // Render error details
                    errorFragment.reportError(
                        that.getString(R.string.userinfo_error_hyperlink),
                        that.getString(R.string.userinfo_error_dialogtitle),
                        uiError)
                }
            }
        }
    }
}
