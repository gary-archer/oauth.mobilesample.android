package com.authsamples.basicmobileapp.views.userinfo

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.FetchCacheKeys
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.api.client.FetchOptions
import com.authsamples.basicmobileapp.api.entities.ApiUserInfo
import com.authsamples.basicmobileapp.api.entities.OAuthUserInfo
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator
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
    val fetchClient: FetchClient,
    val eventBus: EventBus,
    val viewModelCoordinator: ViewModelCoordinator,
    val app: Application
) : AndroidViewModel(app), Observable {

    // Observable data for which the UI must be notified upon change
    private var oauthUserInfo: OAuthUserInfo? = null
    private var apiUserInfo: ApiUserInfo? = null
    var error: UIError? = null
    private val callbacks = PropertyChangeRegistry()

    /*
     * A method to do the work of calling the API
     */
    fun callApi(options: ViewLoadOptions?) {

        val oauthFetchOptions = FetchOptions(
            FetchCacheKeys.OAUTHUSERINFO,
            options?.forceReload ?: false,
            options?.causeError ?: false
        )

        val apiFetchOptions = FetchOptions(
            FetchCacheKeys.APIUSERINFO,
            options?.forceReload ?: false,
            options?.causeError ?: false
        )

        // Initialize state
        this.viewModelCoordinator.onUserInfoViewModelLoading()
        this.resetError()

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

                        // Send a notification if any data loaded
                        if (oauthUserData != null || apiUserData != null) {
                            that.viewModelCoordinator.onUserInfoViewModelLoaded()
                        }
                    }
                }

            } catch (ex: Throwable) {

                // Report errors
                val uiError = ErrorFactory().fromException(ex)
                withContext(Dispatchers.Main) {
                    that.updateError(uiError)
                    that.viewModelCoordinator.onUserInfoViewModelLoaded()
                }
            }
        }
    }

    /*
     * Markup calls this method to get the logged in user's display name
     */
    fun getLoggedInUser(): String {

        if (this.oauthUserInfo == null) {
            return ""
        }

        return "${this.oauthUserInfo!!.givenName} ${this.oauthUserInfo!!.familyName}"
    }

    /*
     * Data to pass when invoking the child error summary view
     */
    fun errorSummaryData(): ErrorSummaryViewModelData {
        return ErrorSummaryViewModelData(
            hyperlinkText = app.getString(R.string.userinfo_error_hyperlink),
            dialogTitle = app.getString(R.string.userinfo_error_dialogtitle),
            error = this.error
        )
    }

    /*
     * Clear user info when we log out and inform the binding system
     */
    fun clearUserInfo() {
        this.oauthUserInfo = null
        this.apiUserInfo = null
        callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.remove(callback)
    }

    /*
     * Set user info and inform the binding system
     */
    private fun updateOAuthUserInfo(oauthUserData: OAuthUserInfo?) {

        if (oauthUserData != null) {
            this.oauthUserInfo = oauthUserData
            this.callbacks.notifyCallbacks(this, 0, null)
        }
    }

    /*
     * Set user info and inform the binding system
     */
    private fun updateApiUserInfo(apiUserData: ApiUserInfo?) {

        if (apiUserData != null) {
            this.apiUserInfo = apiUserData
            this.callbacks.notifyCallbacks(this, 0, null)
        }
    }

    /*
     * Set an error state and blank out data
     */
    private fun updateError(error: UIError?) {
        this.oauthUserInfo = null
        this.apiUserInfo = null
        this.error = error
        this.callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Reset any current errors before attempting to get user info
     */
    private fun resetError() {
        this.error = null
        this.callbacks.notifyCallbacks(this, 0, null)
    }
}
