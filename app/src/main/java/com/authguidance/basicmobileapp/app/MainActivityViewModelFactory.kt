package com.authguidance.basicmobileapp.app

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/*
 * A factory class to create the view model with custom dependencies
 */
class MainActivityViewModelFactory constructor(
    private val app: Application,
    private val events: MainActivityEvents
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        return MainActivityViewModel(this.app, this.events) as T
    }
}
