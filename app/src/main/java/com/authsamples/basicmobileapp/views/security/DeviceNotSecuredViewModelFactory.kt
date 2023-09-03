package com.authsamples.basicmobileapp.views.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.greenrobot.eventbus.EventBus

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class DeviceNotSecuredViewModelFactory(
    private val eventBus: EventBus
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DeviceNotSecuredViewModel(eventBus) as T
    }
}
