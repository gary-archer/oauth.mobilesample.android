package com.authguidance.basicmobileapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/*
 * A factory class to create the view model with custom dependencies
 */
class MainActivitySharedViewModelFactory constructor(
    private val mainModel: MainActivityViewModel,
    private val events: MainActivityEvents,
    private val isInLoginRequired: () -> Boolean
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        return MainActivitySharedViewModel(this.mainModel, events, isInLoginRequired) as T
    }
}
