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
    val apiClient: ApiClient,
    val apiViewEvents: ApiViewEvents
) : BaseObservable() {

    // The data once received
    private var userInfo: UserInfo? = null

    /*
     * A method to do the work of calling the API
     */
    fun callApi(
        options: UserInfoLoadOptions,
        onError: (UIError) -> Unit
    ) {

        // Return if we already have user info, unless we are doing a reload
        if (!options.isInMainView || (this.isLoaded() && !options.reload)) {
            this.apiViewEvents.onViewLoaded(VIEW_USERINFO)
            return
        }

        // Indicate a loading state
        this.apiViewEvents.onViewLoading(VIEW_USERINFO)

        // Make the remote call on a background thread
        val that = this@UserInfoViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val apiClient = that.apiClient
                val requestOptions = ApiRequestOptions(options.causeError)
                val userInfo = apiClient.getUserInfo(requestOptions)

                // Indicate success
                withContext(Dispatchers.Main) {
                    that.setUserInfo(userInfo)
                    that.apiViewEvents.onViewLoaded(VIEW_USERINFO)
                }
            } catch (uiError: UIError) {

                // Inform the view so that the error can be reported
                withContext(Dispatchers.Main) {
                    that.apiViewEvents.onViewLoadFailed(VIEW_USERINFO, uiError)
                    onError(uiError)
                    that.clearUserInfo()
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
     * Clear user info when we log out and inform the binding system
     */
    fun clearUserInfo() {

        this.userInfo = null
        this.notifyChange()
    }

    /*
     * Set user info and inform the binding system
     */
    private fun setUserInfo(userInfo: UserInfo) {

        this.userInfo = userInfo
        this.notifyChange()
    }

    /*
     * Determine whether we need to load data
     */
    private fun isLoaded(): Boolean {
        return this.userInfo != null
    }
}
