package com.authsamples.basicmobileapp.views.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class ErrorSummaryViewModelFactory(
    val onShowDetails: (dialogTitle: String, error: UIError) -> Unit
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ErrorSummaryViewModel(onShowDetails) as T
    }
}
