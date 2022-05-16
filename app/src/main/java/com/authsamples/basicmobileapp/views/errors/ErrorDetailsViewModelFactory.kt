package com.authsamples.basicmobileapp.views.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class ErrorDetailsViewModelFactory(
    val title: String,
    val error: UIError,
    val onDismissCallback: () -> Unit
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ErrorDetailsViewModel(title, error, onDismissCallback) as T
    }
}
