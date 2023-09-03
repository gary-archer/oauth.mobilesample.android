package com.authsamples.basicmobileapp.views.headings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.greenrobot.eventbus.EventBus

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class SessionViewModelFactory(
    private val sessionId: String,
    private val eventBus: EventBus,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionViewModel(sessionId, eventBus, app) as T
    }
}
