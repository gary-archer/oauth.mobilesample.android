package com.authsamples.basicmobileapp.views.companies

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class CompaniesViewModelFactory(
    private val fetchClient: FetchClient,
    private val viewModelCoordinator: ViewModelCoordinator,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CompaniesViewModel(fetchClient, viewModelCoordinator, app) as T
    }
}
