package com.authguidance.basicmobileapp.views.userinfo

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.api.entities.UserInfo
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.utilities.ApiViewEvents
import com.authguidance.basicmobileapp.views.utilities.Constants.VIEW_USERINFO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the user info view
 */
class UserInfoViewModel(
    val apiClientAccessor: () -> ApiClient?,
    val apiViewEvents: ApiViewEvents,
    val shouldLoadAccessor: () -> Boolean
) : BaseObservable() {

    // The data once received
    private var userInfo: UserInfo? = null

    /*
     * A method to do the work of calling the API
     */
    fun callApi(
        options: ApiRequestOptions,
        onError: (UIError) -> Unit
    ) {

        // Only load if conditions are valid
        if (!this.shouldLoadAccessor()) {
            return
        }

        // Indicate a loading state
        this.apiViewEvents.onViewLoading(VIEW_USERINFO)

        // Make the remote call on a background thread
        val that = this@UserInfoViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val apiClient = that.apiClientAccessor()!!
                val userInfo = apiClient.getUserInfo(options)

                // Indicate success
                withContext(Dispatchers.Main) {
                    that.setUserInfo(userInfo)
                    that.apiViewEvents.onViewLoaded(VIEW_USERINFO)
                }
            } catch (uiError: UIError) {

                // Inform the view so that the error can be reported
                withContext(Dispatchers.Main) {
                    that.setUserInfo(null)
                    that.apiViewEvents.onViewLoadFailed(VIEW_USERINFO, uiError)
                    onError(uiError)
                }
            }
        }
    }

    /*
     * Markup calls this method to get the logged in user's full name
     */
    fun getLoggedInUser(): String {

        if (this.userInfo == null) {
            return ""
        }

        return "${this.userInfo!!.givenName} ${this.userInfo!!.familyName}"
    }

    /*
     * Update claims and inform the binding system
     */
    fun setUserInfo(userInfo: UserInfo?) {

        this.userInfo = userInfo
        this.notifyChange()
    }
}
