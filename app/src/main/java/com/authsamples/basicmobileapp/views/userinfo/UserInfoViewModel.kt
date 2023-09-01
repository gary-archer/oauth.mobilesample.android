package com.authsamples.basicmobileapp.views.userinfo

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.api.client.FetchOptions
import com.authsamples.basicmobileapp.api.entities.ApiUserInfo
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.authsamples.basicmobileapp.plumbing.oauth.OAuthUserInfo
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.utilities.Constants.VIEW_USERINFO
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the user info view
 */
class UserInfoViewModel(
    val authenticator: Authenticator,
    val fetchClient: FetchClient,
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

        // Return if we already have user info, unless we are doing a reload
        if (this.isLoaded() && options?.forceReload != true) {
            this.viewModelCoordinator.onViewLoaded(VIEW_USERINFO)
            return
        }

        // Initialize state
        this.viewModelCoordinator.onViewLoading(VIEW_USERINFO)
        this.resetError()

        // Make the remote call on a background thread
        val that = this@UserInfoViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Initialize
                val fetchOptions = FetchOptions(options?.causeError ?: false)

                // Prevent an error on one thread causing a cancellation on the other, and hence a crash
                // Use async in the below calls, to catch error info in this thread
                supervisorScope {

                    // Get OAuth user information from the authorization server
                    val oauthUserInfoTask = CoroutineScope(Dispatchers.IO).async { that.authenticator.getUserInfo() }

                    // Also get user attributes stored in the API's data
                    val apiUserInfoTask = CoroutineScope(Dispatchers.IO).async {
                        that.fetchClient.getUserInfo(fetchOptions)
                    }

                    // Run tasks in parallel and wait for them both to complete
                    val results = awaitAll(oauthUserInfoTask, apiUserInfoTask)
                    val oauthUserData = results[0] as OAuthUserInfo
                    val apiUserData = results[1] as ApiUserInfo

                    // Update the view model on success
                    withContext(Dispatchers.Main) {
                        that.updateData(oauthUserData, apiUserData, null)
                        that.viewModelCoordinator.onViewLoaded(VIEW_USERINFO)
                    }
                }

            } catch (ex: Throwable) {

                // Report errors
                val uiError = ErrorFactory().fromException(ex)
                withContext(Dispatchers.Main) {
                    that.updateData(null, null, uiError)
                    that.viewModelCoordinator.onViewLoadFailed(VIEW_USERINFO, uiError)
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
    fun errorSummaryViewModel(): ErrorSummaryViewModelData {
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
    private fun updateData(oauthUserInfo: OAuthUserInfo?, apiUserInfo: ApiUserInfo?, error: UIError?) {
        this.oauthUserInfo = oauthUserInfo
        this.apiUserInfo = apiUserInfo
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

    /*
     * Determine whether we need to load data
     */
    private fun isLoaded(): Boolean {
        return this.oauthUserInfo != null && this.apiUserInfo != null
    }
}
