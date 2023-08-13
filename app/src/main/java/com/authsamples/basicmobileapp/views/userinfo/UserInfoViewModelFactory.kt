package com.authsamples.basicmobileapp.views.userinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.views.utilities.ApiViewEvents

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class UserInfoViewModelFactory(
    private val authenticator: com.authsamples.basicmobileapp.plumbing.oauth.Authenticator,
    private val apiClient: ApiClient,
    private val apiViewEvents: ApiViewEvents
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return UserInfoViewModel(authenticator, apiClient, apiViewEvents) as T
    }
}
