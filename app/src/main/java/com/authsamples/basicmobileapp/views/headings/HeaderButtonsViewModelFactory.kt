package com.authsamples.basicmobileapp.views.headings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.greenrobot.eventbus.EventBus

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class HeaderButtonsViewModelFactory(
    private val eventBus: EventBus
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HeaderButtonsViewModel(eventBus) as T
    }
}
