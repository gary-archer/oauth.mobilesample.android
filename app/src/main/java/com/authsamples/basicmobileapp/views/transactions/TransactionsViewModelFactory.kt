package com.authsamples.basicmobileapp.views.transactions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class TransactionsViewModelFactory(
    private val apiClient: ApiClient,
    private val viewModelCoordinator: ViewModelCoordinator,
    private val companyId: String,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(apiClient, viewModelCoordinator, companyId, app) as T
    }
}
