package com.authsamples.basicmobileapp.views.transactions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator
import org.greenrobot.eventbus.EventBus

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class TransactionsViewModelFactory(
    private val fetchClient: FetchClient,
    private val eventBus: EventBus,
    private val viewModelCoordinator: ViewModelCoordinator,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(fetchClient, eventBus, viewModelCoordinator, app) as T
    }
}
