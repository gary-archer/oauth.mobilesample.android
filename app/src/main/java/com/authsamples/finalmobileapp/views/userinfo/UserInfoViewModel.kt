package com.authsamples.finalmobileapp.views.userinfo

import android.text.TextUtils
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.authsamples.finalmobileapp.api.client.FetchCacheKeys
import com.authsamples.finalmobileapp.api.client.FetchClient
import com.authsamples.finalmobileapp.api.client.FetchOptions
import com.authsamples.finalmobileapp.api.entities.ApiUserInfo
import com.authsamples.finalmobileapp.api.entities.OAuthUserInfo
import com.authsamples.finalmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.views.utilities.ViewLoadOptions
import com.authsamples.finalmobileapp.views.utilities.ViewModelCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/*
 * A simple view model class for the user info view
 */
class UserInfoViewModel(
    private val fetchClient: FetchClient,
    val eventBus: EventBus,
    private val viewModelCoordinator: ViewModelCoordinator,
) : ViewModel() {

    // Observable data for which the UI must be notified upon change
    private var oauthUserInfo: MutableState<OAuthUserInfo?> = mutableStateOf(null)
    private var apiUserInfo: MutableState<ApiUserInfo?> = mutableStateOf(null)
    var error: MutableState<UIError?> = mutableStateOf(null)

    /*
     * A method to do the work of calling the API
     */
    fun callApi(options: ViewLoadOptions?) {

        val oauthFetchOptions = FetchOptions(
            FetchCacheKeys.OAUTHUSERINFO,
            options?.forceReload ?: false,
            options?.causeError ?: false,
        )

        val apiFetchOptions = FetchOptions(
            FetchCacheKeys.APIUSERINFO,
            options?.forceReload ?: false,
            options?.causeError ?: false,
        )

        // Initialize state
        this.viewModelCoordinator.onUserInfoViewModelLoading()
        this.updateError(null)

        // Make the remote call on a background thread
        val that = this@UserInfoViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {

                // Prevent an error on one thread causing a cancellation on the other, and hence a crash
                // Also use async in the below calls, to catch error info in this calling thread
                supervisorScope {

                    // Get OAuth user information from the authorization server
                    val oauthUserInfoTask = CoroutineScope(Dispatchers.IO).async {
                        that.fetchClient.getOAuthUserInfo(oauthFetchOptions)
                    }

                    // Also get user attributes stored in the API's data
                    val apiUserInfoTask = CoroutineScope(Dispatchers.IO).async {
                        that.fetchClient.getApiUserInfo(apiFetchOptions)
                    }

                    // Run tasks in parallel and wait for them both to complete
                    val results = awaitAll(oauthUserInfoTask, apiUserInfoTask)
                    val oauthUserData = results[0] as OAuthUserInfo?
                    val apiUserData = results[1] as ApiUserInfo?

                    // Update the view model on success
                    withContext(Dispatchers.Main) {

                        // Update data
                        if (oauthUserData != null) {
                            that.updateOAuthUserInfo(oauthUserData)
                        }
                        if (apiUserData != null) {
                            that.updateApiUserInfo(apiUserData)
                        }
                    }
                }

            } catch (ex: Throwable) {

                // Report errors
                val uiError = ErrorFactory().fromException(ex)
                withContext(Dispatchers.Main) {
                    that.updateError(uiError)
                }

            } finally {

                // Indicate loaded
                withContext(Dispatchers.Main) {
                    that.viewModelCoordinator.onUserInfoViewModelLoaded()
                }
            }
        }
    }

    /*
     * Get the logged in user's display name
     */
    fun getUserName(): String {

        if (this.oauthUserInfo.value == null) {
            return ""
        }

        val givenName = this.oauthUserInfo.value?.givenName ?: ""
        val familyName = this.oauthUserInfo.value?.familyName ?: ""
        if (givenName.isBlank() || familyName.isBlank()) {
            return ""
        }

        return "$givenName $familyName"
    }

    /*
     * Get the user's title
     */
    fun getUserTitle(): String {

        if (this.apiUserInfo.value == null) {
            return ""
        }

        return this.apiUserInfo.value?.title ?: ""
    }

    /*
     * Get the user's region details
     */
    fun getUserRegions(): String {

        if (this.apiUserInfo.value == null) {
            return ""
        }

        val regions = this.apiUserInfo.value?.regions ?: ArrayList()
        if (regions.size == 0) {
            return ""
        }

        val regionsText = TextUtils.join(", ", regions)
        return "[$regionsText]"
    }

    /*
     * Clear user info when we log out and inform the binding system
     */
    fun clearUserInfo() {
        this.oauthUserInfo.value = null
        this.apiUserInfo.value = null
    }

    /*
     * Set user info used by the binding system
     */
    private fun updateOAuthUserInfo(oauthUserData: OAuthUserInfo?) {

        if (oauthUserData != null) {
            this.oauthUserInfo.value = oauthUserData
        }
    }

    /*
     * Set user info used by the binding system
     */
    private fun updateApiUserInfo(apiUserData: ApiUserInfo?) {

        if (apiUserData != null) {
            this.apiUserInfo.value = apiUserData
        }
    }

    /*
     * Set error state and blank out data
     */
    private fun updateError(error: UIError?) {

        this.error.value = error
        if (error != null) {
            this.oauthUserInfo.value = null
            this.apiUserInfo.value = null
        }
    }
}
