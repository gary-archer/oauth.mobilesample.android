package com.authsamples.basicmobileapp.views.errors

import androidx.lifecycle.ViewModel
import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * A simple view model class for the error details dialog
 */
class ErrorDetailsViewModel(
    val title: String,
    val error: UIError,
    val onDismissCallback: () -> Unit
) : ViewModel() {

    /*
     * Android binding requires a member function and does not bind to lambdas
     */
    fun onDismiss() {
        this.onDismissCallback()
    }
}
